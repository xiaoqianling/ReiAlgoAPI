package com.rei.algo.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Algorithm {

    private String algoId;          // VARCHAR(16)
    private String userId;          // VARCHAR(8)
    private String title;           // VARCHAR(255)
    private String description;     // TEXT
    private String codeContent;     // LONGTEXT
    private LocalDateTime createdAt;   // DATETIME
    private LocalDateTime updatedAt;   // DATETIME
    private Boolean isPublic;       // BOOLEAN

    // Note: Consider adding a User object here if you often need user details with the algorithm
    // private User user;
} 