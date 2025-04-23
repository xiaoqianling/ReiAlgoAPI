package com.rei.algo.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 帖子数据传输对象 (DTO)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostDTO {

    private String postId;
    private String userId; // Usually set by backend based on logged-in user

    @NotEmpty(message = "帖子标题不能为空")
    @Size(max = 255, message = "标题过长")
    private String title;

    @NotNull(message = "帖子内容不能为空")
    private Object content; // Represents Slate JS Descendants[] (e.g., List<Map<String, Object>>)

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // For receiving tags during creation/update (List of tag names)
    private Set<String> tagNames;

    // For returning details
    private UserDTO user; // Author details
    private List<TagDTO> tags; // Associated tags with details

    // Could add comment count etc.
    // private Integer commentCount;
} 