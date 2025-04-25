package com.rei.algo.DTO.comment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rei.algo.DTO.user.UserDTO;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论数据传输对象 (DTO)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentDTO {

    private String commentId;

    @NotEmpty(message = "帖子ID不能为空") // 在创建时需要
    private String postId;

    private String userId; // 通常由后端设置
    private UserDTO user;  // 评论作者信息 (返回时包含)

    private String parentCommentId; // 可选，用于回复评论

    @NotEmpty(message = "评论内容不能为空")
    private String content;

    private LocalDateTime createdAt;

    // 用于返回嵌套的回复列表
    private List<CommentDTO> replies;

    // 可能需要其他信息，例如点赞数等
    // private Integer likeCount;
} 