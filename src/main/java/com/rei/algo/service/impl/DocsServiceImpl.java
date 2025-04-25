package com.rei.algo.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rei.algo.DTO.docs.DocsDTO;
import com.rei.algo.mapper.DocsMapper;
import com.rei.algo.model.entity.Docs;
import com.rei.algo.service.DocsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocsServiceImpl implements DocsService {
    private final DocsMapper docsMapper;
    private final ObjectMapper objectMapper;

    @Override
    public DocsDTO findById(String id) {
        log.debug("Fetching Docs with id: {}", id);
        Docs docEntity = docsMapper.findById(id);

        if (docEntity == null) {
            log.debug("Docs with id {} not found.", id);
            return null;
        }

        log.debug("Docs entity found: id={}, title={}, createdAt={}, updatedAt={}, content_length={}",
                docEntity.getId(), docEntity.getTitle(), docEntity.getCreatedAt(), docEntity.getUpdatedAt(),
                (docEntity.getContent() != null ? docEntity.getContent().length() : 0));

        Object deserializedContent = convertJsonToContent(docEntity.getContent());

        DocsDTO dto = DocsDTO.builder()
                .id(docEntity.getId())
                .title(docEntity.getTitle())
                .createdAt(docEntity.getCreatedAt())
                .updatedAt(docEntity.getUpdatedAt())
                .content(deserializedContent)
                .build();

        log.debug("Returning DocsDTO: id={}, title={}, createdAt={}, updatedAt={}, content_type={}",
                dto.getId(), dto.getTitle(), dto.getCreatedAt(), dto.getUpdatedAt(),
                (dto.getContent() != null ? dto.getContent().getClass().getName() : "null"));

        return dto;
    }

    private Object convertJsonToContent(String jsonContent) {
        if (!StringUtils.hasText(jsonContent)) {
            return null;
        }
        try {
            return objectMapper.readValue(jsonContent, Object.class);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing docs content from JSON: {}", jsonContent, e);
            return null;
        }
    }
}
