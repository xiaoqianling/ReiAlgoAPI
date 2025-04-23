package com.rei.algo.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper; // For JSON handling
import com.rei.algo.DTO.*;
import com.rei.algo.mapper.PostMapper;
import com.rei.algo.mapper.UserMapper;
import com.rei.algo.model.entity.Post;
import com.rei.algo.model.entity.Tag;
import com.rei.algo.service.PostService;
import com.rei.algo.service.TagService;
import com.rei.algo.util.IDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final TagService tagService; // Inject TagService
    private final ObjectMapper objectMapper; // Inject Jackson ObjectMapper

    @Override
    @Transactional
    public PostDTO createPost(PostCreateRequestDTO postDTO, String creatorUserId) {
        Assert.notNull(postDTO, "Post data cannot be null");
        Assert.hasText(creatorUserId, "Creator User ID cannot be empty");
        Assert.hasText(postDTO.getTitle(), "Post title cannot be empty");
        Assert.notNull(postDTO.getContent(), "Post content cannot be null");

        // 1. Convert content to JSON string
        String contentJson = convertContentToJson(postDTO.getContent());

        // 2. Prepare Post entity
        Post post = new Post();
        BeanUtils.copyProperties(postDTO, post, "content", "tags", "user", "tagNames"); // Exclude fields not in entity or managed differently
        post.setPostId(IDGenerator.generateAlphanumericId());
        post.setUserId(creatorUserId);
        post.setContent(contentJson); // Store JSON string
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        // 3. Insert Post
        postMapper.insert(post);

        // 4. Handle Tags
        if (!CollectionUtils.isEmpty(postDTO.getTagNames())) {
            List<Tag> tags = tagService.findOrCreateTagsByNames(postDTO.getTagNames());
            Set<String> tagIds = tags.stream().map(Tag::getTagId).collect(Collectors.toSet());
            if (!tagIds.isEmpty()) {
                postMapper.addTagsToPost(post.getPostId(), tagIds);
            }
        }

        // 5. Return detailed DTO
        return getPostById(post.getPostId())
               .orElseThrow(() -> new RuntimeException("Failed to fetch created post with ID: " + post.getPostId()));
    }

    @Override
    @Transactional
    public PostDTO updatePost(String postId, PostUpdateRequestDTO postDTO, String currentUserId) {
        Assert.hasText(postId, "Post ID cannot be empty");
        Assert.notNull(postDTO, "Post data cannot be null");
        Assert.hasText(currentUserId, "Current User ID cannot be empty");

        // 1. Find existing post
        Post existingPost = postMapper.findById(postId) // Find basic info first
                .orElseThrow(() -> new RuntimeException("Post not found with ID: " + postId));

        // 2. Check ownership
        // TODO: Add Admin role check if needed
        if (!existingPost.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("User not authorized to update this post");
        }

        // 3. Prepare update entity
        Post postToUpdate = new Post();
        postToUpdate.setPostId(postId);
        boolean needsUpdate = false;

        if (StringUtils.hasText(postDTO.getTitle()) && !postDTO.getTitle().equals(existingPost.getTitle())) {
            postToUpdate.setTitle(postDTO.getTitle());
            needsUpdate = true;
        }
        if (postDTO.getContent() != null) {
             String newContentJson = convertContentToJson(postDTO.getContent());
             // Only update if content actually changed (optional optimization)
             // if (!newContentJson.equals(existingPost.getContent())) {
                postToUpdate.setContent(newContentJson);
                needsUpdate = true;
             // }
        }

        // 4. Update basic info if changed
        if (needsUpdate) {
            // We set updated_at = NOW() in the XML, so no need to set it here
            postMapper.update(postToUpdate);
        }

        // 5. Handle Tags (replace existing tags)
        if (postDTO.getTagNames() != null) { // Allow sending empty list/null to remove all tags
             // Remove existing tags
            postMapper.removeAllTagsFromPost(postId);
             // Add new tags
            if (!CollectionUtils.isEmpty(postDTO.getTagNames())) {
                List<Tag> tags = tagService.findOrCreateTagsByNames(postDTO.getTagNames());
                Set<String> tagIds = tags.stream().map(Tag::getTagId).collect(Collectors.toSet());
                if (!tagIds.isEmpty()) {
                    postMapper.addTagsToPost(postId, tagIds);
                }
            }
        }

        // 6. Return updated detailed DTO
        return getPostById(postId)
               .orElseThrow(() -> new RuntimeException("Failed to fetch updated post with ID: " + postId));
    }

    @Override
    @Transactional
    public void deletePost(String postId, String currentUserId) {
        Assert.hasText(postId, "Post ID cannot be empty");
        Assert.hasText(currentUserId, "Current User ID cannot be empty");

        // 1. Find existing post
        Post existingPost = postMapper.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with ID: " + postId));

        // 2. Check ownership
         // TODO: Add Admin role check if needed
        if (!existingPost.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("User not authorized to delete this post");
        }

        // 3. Delete post (associated tags and comments deleted by DB cascade)
        int deletedRows = postMapper.deleteById(postId);
         if (deletedRows == 0) {
             throw new RuntimeException("Failed to delete post with ID: " + postId);
         }
         // Note: Related post_tag and comment records are deleted due to ON DELETE CASCADE constraint
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PostDTO> getPostById(String postId) {
        Assert.hasText(postId, "Post ID cannot be empty");
        return postMapper.findByIdWithDetails(postId) // Use the mapper method that joins User and selects Tags
                .map(this::convertEntityToDTOWithDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public PageDTO<PostDTO> getPostsByUserId(String userId, int pageNum, int pageSize) {
        Assert.hasText(userId, "User ID cannot be empty");
        validatePageParams(pageNum, pageSize);
        int offset = (pageNum - 1) * pageSize;

        long total = postMapper.countByUserId(userId);
        // Consider joining User info here if needed for list view
        List<Post> posts = postMapper.findByUserId(userId, offset, pageSize);

        List<PostDTO> dtos = posts.stream()
                                .map(this::convertEntityToDTOWithUser) // Convert with basic user info
                                .collect(Collectors.toList());
        long totalPages = (total + pageSize - 1) / pageSize;

        return new PageDTO<>(pageNum, pageSize, total, (int)totalPages, dtos);
    }

    @Override
    @Transactional(readOnly = true)
    public PageDTO<PostDTO> searchPosts(String keyword, int pageNum, int pageSize) {
        validatePageParams(pageNum, pageSize);
        int offset = (pageNum - 1) * pageSize;
        String searchKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;

        // This count might be slow due to JSON search
        long total = postMapper.countByKeyword(searchKeyword);
        // Consider joining User info here
        List<Post> posts = postMapper.search(searchKeyword, offset, pageSize);

        List<PostDTO> dtos = posts.stream()
                                .map(this::convertEntityToDTOWithUser) // Convert with basic user info
                                .collect(Collectors.toList());
        long totalPages = (total + pageSize - 1) / pageSize;

        return new PageDTO<>(pageNum, pageSize, total, (int)totalPages, dtos);
    }

    @Override
    @Transactional(readOnly = true)
    public PageDTO<PostDTO> listAllPosts(int pageNum, int pageSize) {
        validatePageParams(pageNum, pageSize);
        int offset = (pageNum - 1) * pageSize;

        long total = postMapper.countAll();
         // Consider joining User info here
        List<Post> posts = postMapper.findAll(offset, pageSize);

        List<PostDTO> dtos = posts.stream()
                                .map(this::convertEntityToDTOWithUser) // Convert with basic user info
                                .collect(Collectors.toList());
        long totalPages = (total + pageSize - 1) / pageSize;

        return new PageDTO<>(pageNum, pageSize, total, (int)totalPages, dtos);
    }

    // --- Helper Methods --- //

     private void validatePageParams(int pageNum, int pageSize) {
        Assert.isTrue(pageNum >= 1, "Page number must be greater than or equal to 1");
        Assert.isTrue(pageSize >= 1 && pageSize <= 100, "Page size must be between 1 and 100");
    }

    // Convert complex content object to JSON string for storage
    private String convertContentToJson(Object content) {
        try {
            return objectMapper.writeValueAsString(content);
        } catch (JsonProcessingException e) {
            log.error("Error serializing post content to JSON", e);
            throw new RuntimeException("Invalid post content format", e);
        }
    }

    // Convert JSON string from DB back to Object for DTO
    private Object convertJsonToContent(String jsonContent) {
        if (!StringUtils.hasText(jsonContent)) {
            return null; // Or return default empty structure e.g., Collections.emptyList()
        }
        try {
            // Attempt to parse as a generic Object (likely List<Map<String, Object>>)
            return objectMapper.readValue(jsonContent, Object.class);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing post content from JSON: {}", jsonContent, e);
            // Decide how to handle invalid JSON in DB: return null, empty, or throw?
            // Returning the raw string might be an option if frontend can handle it
            return null; // Or maybe return the raw jsonContent?
        }
    }


    // Converts Post entity to DTO, including nested UserDTO and List<TagDTO>
    private PostDTO convertEntityToDTOWithDetails(Post post) {
        if (post == null) return null;
        PostDTO dto = new PostDTO();
        BeanUtils.copyProperties(post, dto, "content", "tags", "user"); // Exclude fields needing special handling

        // Handle Content (JSON String -> Object)
        dto.setContent(convertJsonToContent(post.getContent()));

        // Handle User
        if (post.getUser() != null) {
            UserDTO userDTO = UserDTO.builder()
                    .userId(post.getUser().getUserId())
                    .username(post.getUser().getUsername())
                    .avatarUrl(post.getUser().getAvatarUrl())
                    .build();
            dto.setUser(userDTO);
        }

        // Handle Tags (List<Tag> entity -> List<TagDTO>)
        // Assumes tags are loaded by the findByIdWithDetails query (via nested select)
        if (!CollectionUtils.isEmpty(post.getTags())) {
            dto.setTags(post.getTags().stream()
                    .map(tag -> TagDTO.builder().tagId(tag.getTagId()).name(tag.getName()).build())
                    .collect(Collectors.toList()));
        } else {
             dto.setTags(Collections.emptyList());
        }


        return dto;
    }

     // Converts Post entity to DTO, including only basic User info (for lists)
    private PostDTO convertEntityToDTOWithUser(Post post) {
        if (post == null) return null;
        PostDTO dto = new PostDTO();
        BeanUtils.copyProperties(post, dto, "content", "tags", "user", "tagNames");

        // Handle Content
        dto.setContent(convertJsonToContent(post.getContent()));

         // Fetch and Handle User (if not already joined)
        if (post.getUser() == null && post.getUserId() != null) {
            userMapper.findById(post.getUserId()).ifPresent(user -> {
                UserDTO userDTO = UserDTO.builder()
                                        .userId(user.getUserId())
                                        .username(user.getUsername())
                                        .avatarUrl(user.getAvatarUrl())
                                        .build();
                dto.setUser(userDTO);
            });
        } else if (post.getUser() != null) {
             UserDTO userDTO = UserDTO.builder()
                    .userId(post.getUser().getUserId())
                    .username(post.getUser().getUsername())
                    .avatarUrl(post.getUser().getAvatarUrl())
                    .build();
            dto.setUser(userDTO);
        }


        // Tags usually not needed in list view, set to null or empty
         dto.setTags(null); // Or fetch if needed: tagService.getTagsByPostId(post.getPostId())

        return dto;
    }


    // private Post convertDTOToEntity(PostDTO dto) { ... } // Might be needed
} 