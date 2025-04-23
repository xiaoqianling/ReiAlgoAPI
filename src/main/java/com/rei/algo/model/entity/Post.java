package com.rei.algo.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    private String postId;          // VARCHAR(16)
    private String userId;          // VARCHAR(8)
    private String title;           // VARCHAR(255)
    private String content;         // JSON (Mapped as String initially)
    private LocalDateTime createdAt;   // DATETIME
    private LocalDateTime updatedAt;   // DATETIME

    // Relationships (populated by MyBatis queries)
    private User user;              // Author details (optional)
    private List<Tag> tags;         // Associated tags (optional)
    // Consider adding comment count or recent comments here if frequently needed
    // private int commentCount;
} 