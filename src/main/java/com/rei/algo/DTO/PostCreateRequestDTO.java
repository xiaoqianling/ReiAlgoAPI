package com.rei.algo.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * DTO for creating a new post.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequestDTO {

    @NotBlank(message = "帖子标题不能为空")
    @Size(max = 255, message = "帖子标题不能超过 255 个字符")
    private String title;

    @NotNull(message = "帖子内容不能为空") // Assuming content (JSON) is required
    private Object content; // Assuming content is a JSON structure (e.g., Map or custom object)

    // List of tag names associated with the post
    private Set<String> tagNames;
} 