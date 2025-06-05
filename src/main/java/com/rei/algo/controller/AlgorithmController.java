package com.rei.algo.controller;

import com.rei.algo.DTO.algorithm.AlgorithmDTO;
import com.rei.algo.DTO.PageDTO;
import com.rei.algo.security.SecurityUtils;
import com.rei.algo.service.AlgorithmService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/algorithms")
@RequiredArgsConstructor
@Tag(name = "Algorithm", description = "算法信息管理 API")
public class AlgorithmController {

    private final AlgorithmService algorithmService;

    /**
     * 创建新算法
     * 需要用户已认证
     * @param algorithmDTO 算法信息 DTO
     * @return 创建的算法 DTO
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createAlgorithm(@Valid @RequestBody AlgorithmDTO algorithmDTO) {
        try {
            String creatorUserId = SecurityUtils.getCurrentUserIdOrThrow();
            // Clear user/userId fields from input DTO to avoid confusion
            algorithmDTO.setUserId(null);
            algorithmDTO.setUser(null);
            AlgorithmDTO createdAlgorithm = algorithmService.createAlgorithm(algorithmDTO, creatorUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAlgorithm);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            // Catch specific exceptions from service if possible (e.g., ValidationException)
            return ResponseEntity.badRequest().body("Failed to create algorithm: " + e.getMessage());
        }
    }

    /**
     * 根据 ID 获取算法详情
     * 公开算法对所有认证用户可见，私有算法仅作者可见
     * @param algoId 算法 ID
     * @return 算法 DTO
     */
    @GetMapping("/{algoId}")
    @PreAuthorize("isAuthenticated()") // Basic check, service layer handles public/private logic
    public ResponseEntity<?> getAlgorithmById(@PathVariable String algoId) {
         // Service method handles public/private access logic using current user ID
         String currentUserId = SecurityUtils.getCurrentUserId().orElse(null); // Pass null if not authenticated (though PreAuthorize should prevent this)
        return algorithmService.getAlgorithmById(algoId, currentUserId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        // Catch AccessDeniedException from service layer if needed, map to 403
    }

    /**
     * 更新算法
     * 仅限作者或管理员
     * @param algoId 算法 ID
     * @param algorithmDTO 更新信息 DTO
     * @return 更新后的算法 DTO
     */
    @PutMapping("/{algoId}")
    // PreAuthorize checks ownership based on service logic OR admin role
    // We can rely on the service layer check, or add PreAuthorize for defense-in-depth
    // @PreAuthorize("hasRole('ADMIN') or @algorithmService.getAlgorithmById(#algoId, principal.userId).present") // More complex SpEL
    @PreAuthorize("isAuthenticated()") // Simpler: Let service handle detailed auth
    public ResponseEntity<?> updateAlgorithm(@PathVariable String algoId, @Valid @RequestBody AlgorithmDTO algorithmDTO) {
        try {
            String currentUserId = SecurityUtils.getCurrentUserIdOrThrow();
            // Clear fields that shouldn't be updated directly via this DTO
            algorithmDTO.setUserId(null);
            algorithmDTO.setUser(null);
            algorithmDTO.setAlgoId(null); // ID comes from path variable

            AlgorithmDTO updatedAlgorithm = algorithmService.updateAlgorithm(algoId, algorithmDTO, currentUserId);
            return ResponseEntity.ok(updatedAlgorithm);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (AccessDeniedException e) {
             return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) { // Catch "not found" or other errors from service
            // Log the exception for debugging
            // logger.error("Error updating algorithm {}", algoId, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 删除算法
     * 仅限作者或管理员
     * @param algoId 算法 ID
     * @return No Content
     */
    @DeleteMapping("/{algoId}")
    @PreAuthorize("isAuthenticated()") // Let service handle detailed auth
    public ResponseEntity<?> deleteAlgorithm(@PathVariable String algoId) {
        try {
            String currentUserId = SecurityUtils.getCurrentUserIdOrThrow();
            algorithmService.deleteAlgorithm(algoId, currentUserId);
            return ResponseEntity.noContent().build(); // HTTP 204 No Content
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (AccessDeniedException e) {
             return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) { // Catch "not found" or other errors from service
            // logger.error("Error deleting algorithm {}", algoId, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 获取指定用户的算法列表 (分页)
     * @param userId 用户 ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()") // Anyone authenticated can view user's algorithms (assuming public/private handled in service)
    public ResponseEntity<PageDTO<AlgorithmDTO>> getAlgorithmsByUserId(
            @PathVariable String userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        // TODO: Consider if only owner/admin can see private algorithms in this list - Service layer should handle this
        PageDTO<AlgorithmDTO> page = algorithmService.getAlgorithmsByUserId(userId, pageNum, pageSize);
        return ResponseEntity.ok(page);
    }

    /**
     * 搜索公开算法 (分页)
     * @param keyword 关键字
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()") // Or permitAll()? Depends if guests can search
    public ResponseEntity<PageDTO<AlgorithmDTO>> searchPublicAlgorithms(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageDTO<AlgorithmDTO> page = algorithmService.searchPublicAlgorithms(keyword, pageNum, pageSize);
        return ResponseEntity.ok(page);
    }

     /**
     * 获取所有公开算法列表 (分页)
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    @GetMapping("/public")
     @PreAuthorize("isAuthenticated()") // Or permitAll()?
    public ResponseEntity<PageDTO<AlgorithmDTO>> listPublicAlgorithms(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageDTO<AlgorithmDTO> page = algorithmService.listPublicAlgorithms(pageNum, pageSize);
        return ResponseEntity.ok(page);
    }
} 