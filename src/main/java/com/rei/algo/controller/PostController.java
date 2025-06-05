package com.rei.algo.controller;

import com.rei.algo.DTO.post.PostCreateRequestDTO;
import com.rei.algo.DTO.post.PostSummaryDTO;
import com.rei.algo.DTO.post.PostUpdateRequestDTO;
import com.rei.algo.DTO.PageDTO;
import com.rei.algo.DTO.post.PostDTO;
import com.rei.algo.security.SecurityUtils;
import com.rei.algo.service.PostService;
import com.rei.algo.model.enums.EvaluationType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Posts", description = "社区帖子相关 API")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;

    /**
     * 创建新帖子。
     * 需要用户认证。
     */
    @Operation(summary = "创建新帖子", description = "发布一个新的社区帖子，需要用户登录。",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201", description = "帖子创建成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostDTO.class)))
    @ApiResponse(responseCode = "400", description = "请求无效（例如缺少标题、内容格式错误）")
    @ApiResponse(responseCode = "401", description = "未认证")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostDTO> createPost(@Valid @RequestBody PostCreateRequestDTO createRequest) {
        String creatorUserId = SecurityUtils.getCurrentUserIdOrThrow();
        log.info("User ID '{}' attempting to create post with title starting with: '{}'",
                 creatorUserId,
                 createRequest.getTitle() != null ? createRequest.getTitle().substring(0, Math.min(createRequest.getTitle().length(), 20)) + "..." : "null");
        try {
            System.out.println("creatorUserId: " + creatorUserId);
            PostDTO createdPost = postService.createPost(createRequest, creatorUserId);
            log.info("Post created successfully with ID '{}' by user '{}'", createdPost.getPostId(), creatorUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
        } catch (Exception e) {
            log.error("Error creating post for user '{}' with title starting with '{}': {}",
                      creatorUserId,
                      createRequest.getTitle() != null ? createRequest.getTitle().substring(0, Math.min(createRequest.getTitle().length(), 20)) + "..." : "null",
                      e.getMessage(),
                      e);
            throw e;
        }
    }

    /**
     * 根据 ID 获取帖子详情。
     * 允许匿名访问。
     *
     * @param postId 帖子 ID。
     * @return 成功时返回 200 OK 和帖子 DTO (包含作者和标签详情)。
     *         如果帖子未找到，返回 404 Not Found。
     */
    @Operation(summary = "获取帖子详情", description = "根据帖子 ID 获取单个帖子的详细信息，包括作者和标签。")
    @ApiResponse(responseCode = "200", description = "成功获取帖子详情", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostDTO.class)))
    @ApiResponse(responseCode = "404", description = "帖子未找到")
    @GetMapping("/{postId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PostDTO> getPostById(@Parameter(description = "要获取的帖子ID") @PathVariable String postId) {
        PostDTO post = postService.getPostById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found with ID: " + postId));
        return ResponseEntity.ok(post);
    }

    /**
     * 更新帖子信息。
     * 仅限作者或管理员操作。
     *
     * @param postId 要更新的帖子 ID。
     * @param updateRequest 包含要更新的字段的 DTO (title, content, tagNames)。
     * @return 成功时返回 200 OK 和更新后的帖子 DTO。
     *         权限不足返回 403 Forbidden。
     *         帖子不存在返回 404 Not Found。
     *         输入无效或内容处理失败返回 400 Bad Request。
     */
    @Operation(summary = "更新帖子", description = "更新指定 ID 的帖子信息，仅限帖子创建者或管理员。",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostDTO.class)))
    @ApiResponse(responseCode = "400", description = "请求无效（例如内容格式错误）")
    @ApiResponse(responseCode = "401", description = "未认证")
    @ApiResponse(responseCode = "403", description = "无权限更新此帖子")
    @ApiResponse(responseCode = "404", description = "要更新的帖子未找到")
    @PutMapping("/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostDTO> updatePost(
            @Parameter(description = "要更新的帖子ID") @PathVariable String postId,
            @Valid @RequestBody PostUpdateRequestDTO updateRequest) {

        String currentUserId = SecurityUtils.getCurrentUserIdOrThrow();

        PostDTO updatedPost = postService.updatePost(postId, updateRequest, currentUserId);
        return ResponseEntity.ok(updatedPost);
    }

    /**
     * 删除帖子。
     * 仅限作者或管理员操作。
     *
     * @param postId 要删除的帖子 ID。
     * @return 成功时返回 204 No Content。
     *         权限不足返回 403 Forbidden。
     *         帖子不存在返回 404 Not Found。
     */
    @Operation(summary = "删除帖子", description = "删除指定 ID 的帖子，仅限帖子创建者或管理员。",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "删除成功")
    @ApiResponse(responseCode = "401", description = "未认证")
    @ApiResponse(responseCode = "403", description = "无权限删除此帖子")
    @ApiResponse(responseCode = "404", description = "要删除的帖子未找到")
    @DeleteMapping("/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePost(@Parameter(description = "要删除的帖子ID") @PathVariable String postId) {
        String currentUserId = SecurityUtils.getCurrentUserIdOrThrow();
        postService.deletePost(postId, currentUserId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 获取指定用户的帖子列表（分页）。
     * 允许匿名访问。
     *
     * @param userId 用户 ID。
     * @param pageNum 页码 (从1开始)。
     * @param pageSize 每页数量。
     * @return 成功时返回 200 OK 和分页的帖子 DTO 列表。
     */
    @Operation(summary = "获取用户的帖子列表", description = "获取指定用户发布的所有帖子（分页）。")
    @ApiResponse(responseCode = "200", description = "成功获取列表")
    @GetMapping("/user/{userId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PageDTO<PostSummaryDTO>> getPostsByUserId(
            @Parameter(description = "要查询的用户ID") @PathVariable String userId,
            @Parameter(description = "页码 (从1开始)") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        PageDTO<PostSummaryDTO> page = postService.getPostsByUserId(userId, pageNum, pageSize);
        return ResponseEntity.ok(page);
    }

    /**
     * 搜索帖子（分页）。
     * 允许匿名访问。
     *
     * @param keyword 搜索关键字 (标题或内容)。
     * @param pageNum 页码 (从1开始)。
     * @param pageSize 每页数量。
     * @return 成功时返回 200 OK 和分页的帖子 DTO 列表。
     */
    @Operation(summary = "搜索帖子", description = "根据关键字搜索帖子标题或内容。")
    @ApiResponse(responseCode = "200", description = "成功获取列表")
    @GetMapping("/search")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PageDTO<PostSummaryDTO>> searchPosts(
            @Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码 (从1开始)") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        PageDTO<PostSummaryDTO> page = postService.searchPosts(keyword, pageNum, pageSize);
        return ResponseEntity.ok(page);
    }

    /**
     * 获取所有帖子列表（分页）。
     * 允许匿名访问。
     *
     * @param pageNum 页码 (从1开始)。
     * @param pageSize 每页数量。
     * @return 成功时返回 200 OK 和分页的帖子 DTO 列表。
     */
    @Operation(summary = "获取帖子列表", description = "获取所有帖子的列表（分页）。")
    @ApiResponse(responseCode = "200", description = "成功获取列表")
    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<PageDTO<PostSummaryDTO>> listAllPosts(
            @Parameter(description = "页码 (从1开始)") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        PageDTO<PostSummaryDTO> page = postService.listAllPosts(pageNum, pageSize);
        return ResponseEntity.ok(page);
    }

    /**
     * 获取所有帖子梗概列表（分页） - 使用 getPostSummaries 服务方法。
     * 允许匿名访问。
     */
    @Operation(summary = "获取帖子梗概列表", description = "获取所有帖子的梗概列表（分页），功能同 GET /api/posts。")
    @ApiResponse(responseCode = "200", description = "成功获取列表")
    @GetMapping("/summaries")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PageDTO<PostSummaryDTO>> getPostSummaries(
            @Parameter(description = "页码 (从1开始)") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        // Note: This currently delegates to listAllPosts in the service layer
        PageDTO<PostSummaryDTO> page = postService.getPostSummaries(pageNum, pageSize);
        return ResponseEntity.ok(page);
    }

    /**
     * 增加帖子浏览量。
     * 不需要认证（通常浏览行为不强制登录），但可以根据需要添加。
     * 返回 204 No Content 表示成功处理，即使 view count 可能由于并发没有立即更新。
     */
    @Operation(summary = "增加帖子浏览量", description = "为指定帖子增加一次浏览计数。")
    @ApiResponse(responseCode = "204", description = "浏览量增加请求已处理")
    @ApiResponse(responseCode = "404", description = "帖子未找到")
    @PostMapping("/{postId}/view")
    @PreAuthorize("permitAll()") // Or isAuthenticated() if views should only count for logged-in users
    public ResponseEntity<Void> incrementPostView(@Parameter(description = "帖子ID") @PathVariable String postId) {
        try {
            postService.incrementView(postId);
            // Return 204 even if the count wasn't updated due to race condition/deletion, as the attempt was made.
            return ResponseEntity.noContent().build();
        } catch (RuntimeException ex) {
             // Handle specific exceptions like ResourceNotFoundException if defined
             if (ex.getMessage() != null && ex.getMessage().contains("not found")) {
                 throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
             }
             // Re-throw other unexpected exceptions
             throw ex;
        }
    }

    /**
     * 对帖子进行评价（点赞/点踩）。
     * 需要用户认证。
     * 请求体应包含评价类型 ("LIKE" 或 "DISLIKE")。
     */
    @Operation(summary = "评价帖子", description = "对指定帖子进行点赞或点踩。需要用户登录。",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "评价成功")
    @ApiResponse(responseCode = "400", description = "无效的评价类型")
    @ApiResponse(responseCode = "401", description = "未认证")
    @ApiResponse(responseCode = "404", description = "帖子未找到")
    @PostMapping("/{postId}/evaluate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> evaluatePost(
            @Parameter(description = "帖子ID") @PathVariable String postId,
            @Parameter(description = "评价类型 (LIKE 或 DISLIKE)") @RequestBody EvaluationRequest evaluationRequest) {
        String currentUserId = SecurityUtils.getCurrentUserIdOrThrow();
        try {
            postService.evaluatePost(postId, currentUserId, evaluationRequest.evaluationType());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid evaluation type");
        } catch (RuntimeException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("not found")) {
                 throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
             }
             throw ex;
        }
    }

    // Simple record to hold evaluation type from request body
    private record EvaluationRequest(EvaluationType evaluationType) {}

    // TODO: Add endpoint for getting posts by tag?
    // @GetMapping("/tag/{tagId}")
    // @PreAuthorize("permitAll()")
    // public ResponseEntity<PageDTO<PostDTO>> getPostsByTag(...)
} 