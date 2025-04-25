package com.rei.algo.service.impl;

import com.rei.algo.DTO.comment.CommentDTO;
import com.rei.algo.DTO.PageDTO;
import com.rei.algo.DTO.user.UserDTO;
import com.rei.algo.mapper.CommentMapper;
import com.rei.algo.mapper.PostMapper; // Check if post exists
import com.rei.algo.mapper.UserMapper; // Get user info
import com.rei.algo.model.entity.Comment;
import com.rei.algo.service.CommentService;
import com.rei.algo.util.IDGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;

    private static final int MAX_REPLIES_PREVIEW = 3; // 评论列表预览时加载的回复数量

    @Override
    @Transactional
    public CommentDTO createComment(CommentDTO commentDTO, String creatorUserId) {
        Assert.notNull(commentDTO, "Comment data cannot be null");
        Assert.hasText(creatorUserId, "Creator User ID cannot be empty");
        Assert.hasText(commentDTO.getPostId(), "Post ID cannot be empty");
        Assert.hasText(commentDTO.getContent(), "Comment content cannot be empty");

        // 1. Check if Post exists
        if (postMapper.findById(commentDTO.getPostId()).isEmpty()) {
             throw new RuntimeException("Post not found with ID: " + commentDTO.getPostId());
        }

        // 2. Check if Parent Comment exists (if provided)
        if (StringUtils.hasText(commentDTO.getParentCommentId())) {
            // Use findById instead of findByIdWithUser for efficiency check
            if (commentMapper.findByIdWithUser(commentDTO.getParentCommentId()).isEmpty()) { // findById is sufficient but might not exist, using findByIdWithUser for safety
                 throw new RuntimeException("Parent comment not found with ID: " + commentDTO.getParentCommentId());
            }
            // Optional: Check if parent comment belongs to the same post
            // Optional<Comment> parentComment = commentMapper.findByIdWithUser(commentDTO.getParentCommentId());
            // if (parentComment.isPresent() && !parentComment.get().getPostId().equals(commentDTO.getPostId())) {
            //     throw new IllegalArgumentException("Parent comment does not belong to the same post.");
            // }
        }

        // 3. Create Comment entity
        Comment comment = new Comment();
        BeanUtils.copyProperties(commentDTO, comment, "user", "replies"); // Exclude fields not in entity
        comment.setCommentId(IDGenerator.generateAlphanumericId());
        comment.setUserId(creatorUserId);
        comment.setCreatedAt(LocalDateTime.now());

        // 4. Insert comment
        commentMapper.insert(comment);

        // 5. Fetch the created comment with user details to return
        return commentMapper.findByIdWithUser(comment.getCommentId())
                .map(this::convertEntityToDTO) // Convert including user
                .orElseThrow(() -> new RuntimeException("Failed to fetch created comment with ID: " + comment.getCommentId()));
    }

    @Override
    @Transactional
    public CommentDTO updateCommentContent(String commentId, String content, String currentUserId) {
        Assert.hasText(commentId, "Comment ID cannot be empty");
        Assert.hasText(content, "Content cannot be empty");
        Assert.hasText(currentUserId, "Current User ID cannot be empty");

        // 1. Find existing comment
        // Need userId to check ownership
        Comment existingComment = commentMapper.findByIdWithUser(commentId)
                 .orElseThrow(() -> new RuntimeException("Comment not found with ID: " + commentId));

        // 2. Check ownership
         // TODO: Add Admin role check? Check if enough time has passed?
        if (!existingComment.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("User not authorized to update this comment");
        }

        // 3. Update content
        Comment commentToUpdate = new Comment();
        commentToUpdate.setCommentId(commentId);
        commentToUpdate.setContent(content);
        int updatedRows = commentMapper.updateContent(commentToUpdate);
        if (updatedRows == 0) {
             throw new RuntimeException("Failed to update comment content with ID: " + commentId);
        }

        // 4. Fetch updated comment with user details
        return commentMapper.findByIdWithUser(commentId)
                 .map(this::convertEntityToDTO)
                 .orElseThrow(() -> new RuntimeException("Failed to fetch updated comment with ID: " + commentId));

    }

    @Override
    @Transactional
    public void deleteComment(String commentId, String currentUserId) {
        Assert.hasText(commentId, "Comment ID cannot be empty");
        Assert.hasText(currentUserId, "Current User ID cannot be empty");

        // 1. Find existing comment
        Comment existingComment = commentMapper.findByIdWithUser(commentId)
                 .orElseThrow(() -> new RuntimeException("Comment not found with ID: " + commentId));

        // 2. Check ownership or Admin role
         // TODO: Add Admin role check
        if (!existingComment.getUserId().equals(currentUserId)) {
             throw new AccessDeniedException("User not authorized to delete this comment");
        }

        // 3. Delete (Replies deleted by DB cascade)
        int deletedRows = commentMapper.deleteById(commentId);
         if (deletedRows == 0) {
             throw new RuntimeException("Failed to delete comment with ID: " + commentId);
         }
    }

    @Override
    @Transactional(readOnly = true)
    public PageDTO<CommentDTO> getCommentsByPostId(String postId, int pageNum, int pageSize) {
         Assert.hasText(postId, "Post ID cannot be empty");
         validatePageParams(pageNum, pageSize);
         int offset = (pageNum - 1) * pageSize;

         // 1. Get total count of top-level comments
         long total = commentMapper.countTopLevelByPostId(postId);

         // 2. Fetch top-level comments for the current page
         List<Comment> topLevelComments = commentMapper.findTopLevelByPostId(postId, offset, pageSize);

         // 3. Convert to DTOs and fetch limited replies for each
         List<CommentDTO> commentDTOs = topLevelComments.stream()
                 .map(comment -> {
                     CommentDTO dto = convertEntityToDTO(comment);
                     // Fetch limited replies for preview
                     List<Comment> replies = commentMapper.findRepliesByParentId(comment.getCommentId(), 0, MAX_REPLIES_PREVIEW);
                     if (!replies.isEmpty()) {
                         dto.setReplies(replies.stream().map(this::convertEntityToDTO).collect(Collectors.toList()));
                     } else {
                          dto.setReplies(Collections.emptyList());
                     }
                      // Optionally add reply count? Requires another query: commentMapper.countRepliesByParentId(comment.getCommentId())
                     // dto.setReplyCount(commentMapper.countRepliesByParentId(comment.getCommentId()));
                     return dto;
                 })
                 .collect(Collectors.toList());

        long totalPages = (total + pageSize - 1) / pageSize;
        return new PageDTO<>(pageNum, pageSize, total, (int)totalPages, commentDTOs);
    }

     @Override
     @Transactional(readOnly = true)
     public Optional<CommentDTO> getCommentWithReplies(String commentId) {
         Assert.hasText(commentId, "Comment ID cannot be empty");

         return commentMapper.findByIdWithUser(commentId)
                 .map(comment -> {
                     CommentDTO dto = convertEntityToDTO(comment);
                     // Fetch ALL direct replies (could be large - consider pagination here too)
                     // For simplicity, let's fetch all for now. If performance is an issue, paginate this.
                     List<Comment> replies = commentMapper.findRepliesByParentId(commentId, 0, Integer.MAX_VALUE); // Fetch all
                     if (!replies.isEmpty()) {
                         // We won't fetch replies of replies here to avoid complexity/depth issues.
                         // Frontend can make separate requests if needed for deeper levels.
                         dto.setReplies(replies.stream().map(this::convertEntityToDTO).collect(Collectors.toList()));
                     } else {
                         dto.setReplies(Collections.emptyList());
                     }
                     return dto;
                 });
     }


    @Override
    @Transactional(readOnly = true)
    public PageDTO<CommentDTO> getCommentsByUserId(String userId, int pageNum, int pageSize) {
        Assert.hasText(userId, "User ID cannot be empty");
        validatePageParams(pageNum, pageSize);
        int offset = (pageNum - 1) * pageSize;

        long total = commentMapper.countByUserId(userId);
        // Fetch comments with user info (author)
        List<Comment> comments = commentMapper.findByUserId(userId, offset, pageSize);

        // TODO: Add Post title/ID to the DTO for context if needed by joining post table in mapper
        List<CommentDTO> dtos = comments.stream()
                                      .map(this::convertEntityToDTO)
                                      .collect(Collectors.toList());
        long totalPages = (total + pageSize - 1) / pageSize;

        return new PageDTO<>(pageNum, pageSize, total, (int)totalPages, dtos);
    }

    // --- Helper Methods --- //

    private void validatePageParams(int pageNum, int pageSize) {
        Assert.isTrue(pageNum >= 1, "Page number must be greater than or equal to 1");
        Assert.isTrue(pageSize >= 1 && pageSize <= 100, "Page size must be between 1 and 100");
    }

    // Converts Comment entity to DTO, including nested UserDTO
    private CommentDTO convertEntityToDTO(Comment comment) {
        if (comment == null) return null;
        CommentDTO dto = new CommentDTO();
        BeanUtils.copyProperties(comment, dto, "user", "replies"); // Exclude handled fields

        // Handle User
        if (comment.getUser() != null) {
            UserDTO userDTO = UserDTO.builder()
                    .userId(comment.getUser().getUserId())
                    .username(comment.getUser().getUsername())
                    .avatarUrl(comment.getUser().getAvatarUrl())
                    .build();
            dto.setUser(userDTO);
        } else if (comment.getUserId() != null) {
             // If user wasn't joined, fetch minimal info (less efficient)
            userMapper.findById(comment.getUserId()).ifPresent(user -> {
                 UserDTO userDTO = UserDTO.builder()
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .avatarUrl(user.getAvatarUrl())
                    .build();
                 dto.setUser(userDTO);
            });
        }

        // Replies are handled by the calling method (getCommentsByPostId or getCommentWithReplies)
        dto.setReplies(null); // Initialize as null or empty list if preferred

        return dto;
    }

    // private Comment convertDTOToEntity(CommentDTO dto) { ... }
} 