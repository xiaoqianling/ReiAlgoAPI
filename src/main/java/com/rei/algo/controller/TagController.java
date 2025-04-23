package com.rei.algo.controller;

import com.rei.algo.DTO.TagDTO;
import com.rei.algo.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@Tag(name = "Tags", description = "标签相关 API")
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /**
     * 获取所有标签列表。
     * 允许匿名访问。
     *
     * @return 包含所有标签的 TagDTO 列表。
     */
    @Operation(summary = "获取所有标签", description = "获取系统内所有已使用的标签列表。")
    @ApiResponse(responseCode = "200", description = "成功获取标签列表")
    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<TagDTO>> getAllTags() {
        List<TagDTO> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }

    /**
     * 根据帖子 ID 获取该帖子的标签列表。
     * 允许匿名访问。
     *
     * @param postId 帖子 ID。
     * @return 包含该帖子关联的 TagDTO 列表。
     */
    @Operation(summary = "获取帖子的标签", description = "获取指定帖子关联的所有标签。")
    @ApiResponse(responseCode = "200", description = "成功获取标签列表")
    @ApiResponse(responseCode = "404", description = "帖子未找到（如果需要校验帖子存在性）")
    @GetMapping("/post/{postId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<TagDTO>> getTagsByPostId(
            @Parameter(description = "要查询标签的帖子ID") @PathVariable String postId) {
        List<TagDTO> tags = tagService.getTagsByPostId(postId);
        return ResponseEntity.ok(tags);
    }

    // --- Admin Endpoints (Optional, uncomment and implement if needed) ---
    /*
    @Operation(summary = "创建标签（管理员）", description = "直接创建一个新标签，仅限管理员。",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201", description = "标签创建成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TagDTO.class)))
    @ApiResponse(responseCode = "400", description = "请求无效（例如名称为空或已存在）")
    @ApiResponse(responseCode = "401", description = "未认证")
    @ApiResponse(responseCode = "403", description = "无权限")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TagDTO> createTag(@Valid @RequestBody TagDTO tagDTO) {
        TagDTO createdTag = tagService.createTag(tagDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTag);
    }

    @Operation(summary = "删除标签（管理员）", description = "删除指定 ID 的标签，仅限管理员。注意：需要处理标签与帖子的关联。",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "删除成功")
    @ApiResponse(responseCode = "401", description = "未认证")
    @ApiResponse(responseCode = "403", description = "无权限")
    @ApiResponse(responseCode = "404", description = "要删除的标签未找到")
    @DeleteMapping("/{tagId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTag(@Parameter(description = "要删除的标签ID") @PathVariable String tagId) {
         return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
    */
}
