package com.rei.algo.controller;

import com.rei.algo.DTO.comment.CommentDTO;
import com.rei.algo.DTO.PageDTO;
import com.rei.algo.security.SecurityUtils;
import com.rei.algo.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Import Slf4j
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Comments", description = "评论管理相关 API")
@RestController
@RequestMapping("/api/comments")
@Validated
@RequiredArgsConstructor
@Slf4j // Add Slf4j
public class CommentController {

    private final CommentService commentService;

    /**
     * 创建新评论或回复。
     * 需要用户认证。
     *
     * @param commentDTO 包含 postId, content, 和可选的 parentCommentId 的 DTO。
     * @return 成功时返回 201 Created 和创建的评论 DTO (包含作者信息)。
     *         如果帖子或父评论不存在，或输入无效，返回 400 Bad Request。
     */
    @Operation(summary = "创建评论/回复", description = "在指定帖子下创建新评论，或回复已有评论。需要用户登录。",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201", description = "评论创建成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentDTO.class)))
    @ApiResponse(responseCode = "400", description = "请求无效（例如帖子不存在、父评论不存在、内容为空）")
    @ApiResponse(responseCode = "401", description = "未认证")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentDTO> createComment(@Valid @RequestBody CommentDTO commentDTO) {
        String creatorUserId = SecurityUtils.getCurrentUserIdOrThrow();
        // Clear fields set by backend
        commentDTO.setUserId(null);
        commentDTO.setUser(null);
        commentDTO.setCreatedAt(null);
        commentDTO.setReplies(null); // Replies are fetched, not created directly here

        // Let GlobalExceptionHandler handle exceptions (e.g., PostNotFound, ParentCommentNotFound)
        CommentDTO createdComment = commentService.createComment(commentDTO, creatorUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    // Inner DTO for updating comment content
    @Schema(description = "用于更新评论内容的请求体") // Add Schema description
    @Data
    static class UpdateCommentRequest {
        @Schema(description = "新的评论内容", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "评论内容不能为空")
        private String content;
    }

    /**
     * 更新评论内容。
     * 仅限作者操作。
     *
     * @param commentId 要更新的评论 ID。
     * @param request 包含新评论内容的请求体。
     * @return 成功时返回 200 OK 和更新后的评论 DTO。
     *         权限不足返回 403 Forbidden。
     *         评论不存在返回 404 Not Found。
     *         输入无效返回 400 Bad Request。
     */
    @Operation(summary = "更新评论内容", description = "更新指定 ID 的评论内容，仅限评论创建者。",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentDTO.class)))
    @ApiResponse(responseCode = "400", description = "请求无效（内容为空）")
    @ApiResponse(responseCode = "401", description = "未认证")
    @ApiResponse(responseCode = "403", description = "无权限更新此评论")
    @ApiResponse(responseCode = "404", description = "要更新的评论未找到")
    @PutMapping("/{commentId}/content")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentDTO> updateCommentContent(
            @Parameter(description = "要更新的评论ID") @PathVariable String commentId,
            @Valid @RequestBody UpdateCommentRequest request) {

        String currentUserId = SecurityUtils.getCurrentUserIdOrThrow();
        // Service should throw AccessDeniedException or ResourceNotFoundException
        CommentDTO updatedComment = commentService.updateCommentContent(commentId, request.getContent(), currentUserId);
        return ResponseEntity.ok(updatedComment);
    }

    /**
     * 删除评论。
     * 仅限作者或管理员操作。
     *
     * @param commentId 要删除的评论 ID。
     * @return 成功时返回 204 No Content。
     *         权限不足返回 403 Forbidden。
     *         评论不存在返回 404 Not Found。
     */
    @Operation(summary = "删除评论", description = "删除指定 ID 的评论，仅限评论创建者或管理员。",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "删除成功")
    @ApiResponse(responseCode = "401", description = "未认证")
    @ApiResponse(responseCode = "403", description = "无权限删除此评论")
    @ApiResponse(responseCode = "404", description = "要删除的评论未找到")
    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()") // Delegate detailed auth check to service
    public ResponseEntity<Void> deleteComment(@Parameter(description = "要删除的评论ID") @PathVariable String commentId) {
        String currentUserId = SecurityUtils.getCurrentUserIdOrThrow();
        // Service should throw AccessDeniedException or ResourceNotFoundException
        commentService.deleteComment(commentId, currentUserId);
        return ResponseEntity.noContent().build(); // HTTP 204
    }

    /**
     * 根据帖子 ID 获取评论列表（分页）。
     * 包含顶级评论及其有限数量的预览回复。
     * 允许匿名访问。
     *
     * @param postId 帖子 ID。
     * @param pageNum 页码 (从1开始)。
     * @param pageSize 每页数量。
     * @return 成功时返回 200 OK 和分页的评论 DTO 列表。
     */
    @Operation(summary = "获取帖子的评论列表", description = "获取指定帖子下的顶级评论列表（分页），包含部分回复预览。")
    @ApiResponse(responseCode = "200", description = "成功获取列表")
    @GetMapping("/post/{postId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PageDTO<CommentDTO>> getCommentsByPostId(
            @Parameter(description = "要查询评论的帖子ID") @PathVariable String postId,
            @Parameter(description = "页码 (从1开始)") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        PageDTO<CommentDTO> page = commentService.getCommentsByPostId(postId, pageNum, pageSize);
        return ResponseEntity.ok(page);
    }

     /**
     * 根据评论 ID 获取单个评论及其所有直接回复。
     * 允许匿名访问。
     *
     * @param commentId 评论 ID。
     * @return 成功时返回 200 OK 和评论 DTO (包含其直接回复列表)。
     *         如果评论未找到，返回 404 Not Found。
     */
    @Operation(summary = "获取评论及其回复", description = "获取指定 ID 的单个评论及其所有直接回复。")
    @ApiResponse(responseCode = "200", description = "成功获取评论及回复", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentDTO.class)))
    @ApiResponse(responseCode = "404", description = "评论未找到")
    @GetMapping("/{commentId}/with-replies")
    @PreAuthorize("permitAll()")
    public ResponseEntity<CommentDTO> getCommentWithReplies(@Parameter(description = "要获取的评论ID") @PathVariable String commentId) {
        CommentDTO comment = commentService.getCommentWithReplies(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found with ID: " + commentId));
        return ResponseEntity.ok(comment);
    }


    /**
     * 根据用户 ID 获取评论列表（分页）。
     * 需要用户认证。
     *
     * @param userId 用户 ID。
     * @param pageNum 页码 (从1开始)。
     * @param pageSize 每页数量。
     * @return 成功时返回 200 OK 和分页的评论 DTO 列表。
     */
    @Operation(summary = "获取用户的评论列表", description = "获取指定用户发表的所有评论（分页）。可能需要用户认证。",
               security = @SecurityRequirement(name = "bearerAuth")) // Assuming requires auth
    @ApiResponse(responseCode = "200", description = "成功获取列表")
    @ApiResponse(responseCode = "401", description = "未认证")
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()") // Only authenticated users can see someone's comment history? Or permitAll?
    public ResponseEntity<PageDTO<CommentDTO>> getCommentsByUserId(
            @Parameter(description = "要查询的用户ID") @PathVariable String userId,
            @Parameter(description = "页码 (从1开始)") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        PageDTO<CommentDTO> page = commentService.getCommentsByUserId(userId, pageNum, pageSize);
        return ResponseEntity.ok(page);
    }
} 