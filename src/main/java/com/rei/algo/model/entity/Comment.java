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
public class Comment {

    private String commentId;       // VARCHAR(16)
    private String postId;          // VARCHAR(16)
    private String userId;          // VARCHAR(8)
    private String parentCommentId; // VARCHAR(16) (Nullable)
    private String content;         // TEXT
    private LocalDateTime createdAt;   // DATETIME

    // Relationships (populated by MyBatis queries)
    private User user;              // Author details (optional)
    private List<Comment> replies;  // Nested replies (optional, requires specific query logic)

    // Could also include parent comment details if needed
    // private Comment parentComment;
} 