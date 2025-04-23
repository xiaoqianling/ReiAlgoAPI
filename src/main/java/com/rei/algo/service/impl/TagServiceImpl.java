package com.rei.algo.service.impl;

import com.rei.algo.DTO.TagDTO;
import com.rei.algo.mapper.TagMapper;
import com.rei.algo.model.entity.Tag;
import com.rei.algo.service.TagService;
import com.rei.algo.util.IDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j // Use Slf4j for logging
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;

    @Override
    @Transactional
    public TagDTO createTag(TagDTO tagDTO) {
        Assert.notNull(tagDTO, "Tag DTO cannot be null");
        Assert.hasText(tagDTO.getName(), "Tag name cannot be empty");

        String tagName = tagDTO.getName().trim();

        // Find or create the tag
        Tag tag = findOrCreateTagByName(tagName);

        return convertToDTO(tag);
    }

    @Override
    @Transactional // Ensure atomicity for find-or-create
    public Tag findOrCreateTagByName(String tagName) {
        Assert.hasText(tagName, "Tag name cannot be empty");
        String trimmedName = tagName.trim();

        // Try to find existing tag first
        return tagMapper.findByName(trimmedName)
                .orElseGet(() -> {
                    log.info("Tag '{}' not found, creating new one.", trimmedName);
                    Tag newTag = Tag.builder()
                            .tagId(IDGenerator.generateAlphanumericId())
                            .name(trimmedName)
                            .build();
                    try {
                        tagMapper.insert(newTag);
                         return newTag;
                    } catch (Exception e) {
                        // Handle potential race condition: another thread inserted the same tag
                        // between findByName and insert. Re-query the tag.
                        log.warn("Race condition likely occurred while inserting tag '{}'. Re-querying.", trimmedName, e);
                        return tagMapper.findByName(trimmedName)
                                .orElseThrow(() -> new RuntimeException("Failed to find or create tag: " + trimmedName, e)); // Should not happen if insert failed due to duplicate
                    }
                });
    }

    @Override
    @Transactional
    public List<Tag> findOrCreateTagsByNames(Set<String> tagNames) {
        if (CollectionUtils.isEmpty(tagNames)) {
            return Collections.emptyList();
        }

        List<Tag> resultTags = new ArrayList<>();
        List<String> namesToCreate = new ArrayList<>();
        Set<String> trimmedNames = tagNames.stream()
                                          .filter(name -> name != null && !name.trim().isEmpty())
                                          .map(String::trim)
                                          .collect(Collectors.toSet());

        // TODO: Optimize: Find existing tags in one query? (e.g., findByNames)
        // If findByNames is implemented in Mapper:
        // Map<String, Tag> existingTagsMap = tagMapper.findByNames(trimmedNames).stream().collect(Collectors.toMap(Tag::getName, t -> t));
        // resultTags.addAll(existingTagsMap.values());
        // namesToCreate.addAll(trimmedNames.stream().filter(name -> !existingTagsMap.containsKey(name)).toList());

        // Simple approach: find or create one by one (less efficient for many tags)
        for (String name : trimmedNames) {
             resultTags.add(findOrCreateTagByName(name));
        }


        // If namesToCreate is populated by an optimized approach:
        // if (!namesToCreate.isEmpty()) {
        //     List<Tag> newTags = new ArrayList<>();
        //     for (String name : namesToCreate) {
        //         newTags.add(Tag.builder()
        //                        .tagId(IDGenerator.generateAlphanumericId())
        //                        .name(name)
        //                        .build());
        //     }
        //     if (!newTags.isEmpty()) {
        //         try {
        //             // Use insertBatchIfNotExists or handle potential duplicates
        //             tagMapper.insertBatchIfNotExists(newTags); // Assumes this handles duplicates or is safe
        //             resultTags.addAll(newTags); // Need IDs if insertBatch doesn't return them
        //             // Potentially re-query inserted tags if IDs weren't returned/set
        //         } catch (Exception e) {
        //            log.error("Error during batch insert of tags: {}", namesToCreate, e);
        //            // Handle error, maybe retry individually?
        //            // For simplicity now, we rely on the one-by-one findOrCreate
        //         }
        //     }
        // }

        return resultTags;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagDTO> getAllTags() {
        return tagMapper.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagDTO> getTagsByPostId(String postId) {
        Assert.hasText(postId, "Post ID cannot be empty");
        return tagMapper.findTagsByPostId(postId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // --- Helper Methods --- //

    private TagDTO convertToDTO(Tag tag) {
        if (tag == null) return null;
        TagDTO dto = new TagDTO();
        BeanUtils.copyProperties(tag, dto);
        return dto;
    }

    // private Tag convertToEntity(TagDTO dto) { ... }
} 