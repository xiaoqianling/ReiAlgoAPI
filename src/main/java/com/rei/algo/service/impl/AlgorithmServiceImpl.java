package com.rei.algo.service.impl;

import com.rei.algo.DTO.AlgorithmDTO;
import com.rei.algo.DTO.PageDTO;
import com.rei.algo.DTO.UserDTO;
import com.rei.algo.mapper.AlgorithmMapper;
import com.rei.algo.mapper.UserMapper; // 用于获取用户信息
import com.rei.algo.model.entity.Algorithm;
import com.rei.algo.model.entity.User;
import com.rei.algo.service.AlgorithmService;
import com.rei.algo.util.IDGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils; // For simple property copying
import org.springframework.security.access.AccessDeniedException; // Specific exception for authorization failure
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils; // For checking empty strings


import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlgorithmServiceImpl implements AlgorithmService {

    private final AlgorithmMapper algorithmMapper;
    private final UserMapper userMapper; // Inject UserMapper to fetch author details

    @Override
    @Transactional
    public AlgorithmDTO createAlgorithm(AlgorithmDTO algorithmDTO, String creatorUserId) {
        Assert.notNull(algorithmDTO, "Algorithm data cannot be null");
        Assert.hasText(creatorUserId, "Creator User ID cannot be empty");
        Assert.hasText(algorithmDTO.getTitle(), "Algorithm title cannot be empty");
        Assert.hasText(algorithmDTO.getCodeContent(), "Algorithm code content cannot be empty");
        Assert.notNull(algorithmDTO.getIsPublic(), "Visibility (isPublic) must be specified");


        Algorithm algorithm = convertToEntity(algorithmDTO);
        algorithm.setAlgoId(IDGenerator.generateAlphanumericId()); // Generate ID
        algorithm.setUserId(creatorUserId);
        algorithm.setCreatedAt(LocalDateTime.now());
        algorithm.setUpdatedAt(LocalDateTime.now());

        algorithmMapper.insert(algorithm);

        return convertToDTO(algorithm); // Return DTO of the created algorithm
    }

    @Override
    @Transactional
    public AlgorithmDTO updateAlgorithm(String algoId, AlgorithmDTO algorithmDTO, String currentUserId) {
        Assert.hasText(algoId, "Algorithm ID cannot be empty");
        Assert.notNull(algorithmDTO, "Algorithm data cannot be null");
        Assert.hasText(currentUserId, "Current User ID cannot be empty");

        // 1. Find the existing algorithm
        Algorithm existingAlgorithm = algorithmMapper.findById(algoId)
                .orElseThrow(() -> new RuntimeException("Algorithm not found with ID: " + algoId)); // Consider specific exception

        // 2. Check ownership/permission
        if (!existingAlgorithm.getUserId().equals(currentUserId)) {
             // Consider if Admin should bypass this check
             // boolean isAdmin = ... check user role ...
             // if(!isAdmin) {
                 throw new AccessDeniedException("User not authorized to update this algorithm");
             // }
        }

        // 3. Prepare update object (only update allowed fields)
        Algorithm algorithmToUpdate = new Algorithm();
        BeanUtils.copyProperties(algorithmDTO, algorithmToUpdate); // Copy fields from DTO
        algorithmToUpdate.setAlgoId(algoId); // Ensure ID is set for update WHERE clause
        algorithmToUpdate.setUserId(null); // Prevent changing owner
        algorithmToUpdate.setCreatedAt(null); // Prevent changing creation time
        algorithmToUpdate.setUpdatedAt(LocalDateTime.now()); // Set update time


        // 4. Perform partial update using MyBatis dynamic SQL in Mapper
        int updatedRows = algorithmMapper.update(algorithmToUpdate);
         if (updatedRows == 0) {
             throw new RuntimeException("Failed to update algorithm with ID: " + algoId);
         }

        // 5. Fetch updated algorithm and return DTO
        return getAlgorithmById(algoId, currentUserId)
                .orElseThrow(() -> new RuntimeException("Failed to fetch updated algorithm with ID: " + algoId)); // Should exist if updated
    }

    @Override
    @Transactional
    public void deleteAlgorithm(String algoId, String currentUserId) {
        Assert.hasText(algoId, "Algorithm ID cannot be empty");
        Assert.hasText(currentUserId, "Current User ID cannot be empty");

        // 1. Find the existing algorithm
        Algorithm existingAlgorithm = algorithmMapper.findById(algoId)
                .orElseThrow(() -> new RuntimeException("Algorithm not found with ID: " + algoId));

        // 2. Check ownership/permission
        // TODO: Add Admin role check if admins can delete any algorithm
        if (!existingAlgorithm.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("User not authorized to delete this algorithm");
        }

        // 3. Delete
        int deletedRows = algorithmMapper.deleteById(algoId);
         if (deletedRows == 0) {
             // Should not happen if findById succeeded, unless deleted concurrently
             throw new RuntimeException("Failed to delete algorithm with ID: " + algoId);
         }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AlgorithmDTO> getAlgorithmById(String algoId, String currentUserId) {
         Assert.hasText(algoId, "Algorithm ID cannot be empty");

         return algorithmMapper.findById(algoId)
                 .map(algorithm -> {
                     // Check access for private algorithms
                     if (!algorithm.getIsPublic() && !Objects.equals(algorithm.getUserId(), currentUserId)) {
                         // Allow Admin access?
                         // boolean isAdmin = ... check currentUserId's role ...
                         // if (!isAdmin) {
                             throw new AccessDeniedException("User not authorized to view this private algorithm");
                         // }
                     }
                     return convertToDTOWithUser(algorithm); // Convert including user info
                 });
    }

    @Override
    @Transactional(readOnly = true)
    public PageDTO<AlgorithmDTO> getAlgorithmsByUserId(String userId, int pageNum, int pageSize) {
        Assert.hasText(userId, "User ID cannot be empty");
        validatePageParams(pageNum, pageSize);

        int offset = (pageNum - 1) * pageSize;
        long total = algorithmMapper.countByUserId(userId);
        List<Algorithm> algorithms = algorithmMapper.findByUserId(userId, offset, pageSize);

        List<AlgorithmDTO> dtos = algorithms.stream()
                                          .map(this::convertToDTO) // No need for user info here usually
                                          .collect(Collectors.toList());
        long totalPages = (total + pageSize - 1) / pageSize;

        return new PageDTO<>(pageNum, pageSize, total, (int)totalPages, dtos);
    }

    @Override
    @Transactional(readOnly = true)
    public PageDTO<AlgorithmDTO> searchPublicAlgorithms(String keyword, int pageNum, int pageSize) {
        validatePageParams(pageNum, pageSize);
        int offset = (pageNum - 1) * pageSize;
        String searchKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;

        long total = algorithmMapper.countPublicByKeyword(searchKeyword);
        List<Algorithm> algorithms = algorithmMapper.searchPublic(searchKeyword, offset, pageSize);

        List<AlgorithmDTO> dtos = algorithms.stream()
                                          .map(this::convertToDTOWithUser) // Include user info for public lists
                                          .collect(Collectors.toList());
        long totalPages = (total + pageSize - 1) / pageSize;

        return new PageDTO<>(pageNum, pageSize, total, (int)totalPages, dtos);
    }

    @Override
    @Transactional(readOnly = true)
    public PageDTO<AlgorithmDTO> listPublicAlgorithms(int pageNum, int pageSize) {
       validatePageParams(pageNum, pageSize);
       int offset = (pageNum - 1) * pageSize;

       long total = algorithmMapper.countAllPublic();
       List<Algorithm> algorithms = algorithmMapper.findAllPublic(offset, pageSize);

       List<AlgorithmDTO> dtos = algorithms.stream()
                                          .map(this::convertToDTOWithUser) // Include user info for public lists
                                          .collect(Collectors.toList());
       long totalPages = (total + pageSize - 1) / pageSize;

       return new PageDTO<>(pageNum, pageSize, total, (int)totalPages, dtos);
    }

     // --- Helper Methods --- //

    private void validatePageParams(int pageNum, int pageSize) {
        Assert.isTrue(pageNum >= 1, "Page number must be greater than or equal to 1");
        Assert.isTrue(pageSize >= 1 && pageSize <= 100, "Page size must be between 1 and 100"); // Example limit
    }

    private AlgorithmDTO convertToDTO(Algorithm algorithm) {
        if (algorithm == null) return null;
        AlgorithmDTO dto = new AlgorithmDTO();
        BeanUtils.copyProperties(algorithm, dto);
        // Don't include UserDTO here unless specifically needed and fetched
        dto.setUser(null);
        return dto;
    }

     // Converts Entity to DTO and includes basic User info
    private AlgorithmDTO convertToDTOWithUser(Algorithm algorithm) {
        if (algorithm == null) return null;
        AlgorithmDTO dto = convertToDTO(algorithm);

        // Fetch minimal user info if needed
        if (algorithm.getUserId() != null) {
            userMapper.findById(algorithm.getUserId()).ifPresent(user -> {
                UserDTO userDTO = UserDTO.builder()
                                        .userId(user.getUserId())
                                        .username(user.getUsername())
                                        .avatarUrl(user.getAvatarUrl())
                                        .build();
                dto.setUser(userDTO);
            });
        }
        return dto;
    }

    private Algorithm convertToEntity(AlgorithmDTO dto) {
        if (dto == null) return null;
        Algorithm entity = new Algorithm();
        BeanUtils.copyProperties(dto, entity);
        // Ensure fields not in DTO or managed internally are null/default
        entity.setAlgoId(null);
        entity.setUserId(null); // Will be set from security context
        entity.setCreatedAt(null);
        entity.setUpdatedAt(null);
        return entity;
    }
} 