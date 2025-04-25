package com.rei.algo.controller;

import com.rei.algo.DTO.PageDTO;
import com.rei.algo.DTO.user.UserDTO;
import com.rei.algo.DTO.user.UserProfileUpdateDTO;
import com.rei.algo.security.SecurityUtils;
import com.rei.algo.service.UserService;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Users", description = "用户管理相关 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * 获取当前登录用户的信息。
     * 需要有效的认证令牌。
     *
     * @return 成功时返回 200 OK 和当前用户的 UserDTO。
     *         如果用户未找到或认证失败，返回 404 Not Found 或相应的错误状态码 (由异常处理器处理)。
     */
    @Operation(summary = "获取当前用户信息", description = "获取当前已登录用户的详细信息",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "成功获取用户信息", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class)))
    @ApiResponse(responseCode = "401", description = "未认证")
    @ApiResponse(responseCode = "404", description = "当前用户信息未找到")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> getCurrentUser() {
        // 1. 尝试获取当前用户 ID
        String userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> {
                     log.warn("Could not retrieve current user ID from authenticated principal even though authenticated.");
                     // Throw an exception that GlobalExceptionHandler can map to 500 or 401/403
                     return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not determine current user information.");
                 });

        // 2. 使用获取到的 ID 查询用户信息
        UserDTO userDto = userService.getUserById(userId)
                .orElseThrow(() -> {
                     log.warn("User profile not found in DB for authenticated user ID: {}", userId);
                     // Throw an exception mapped to 404 by GlobalExceptionHandler (if handler is configured) or return 404 directly
                     return new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found for current user");
                 });

        // 3. 返回成功响应
        return ResponseEntity.ok(userDto);
    }

    /**
     * 根据用户 ID 获取指定用户信息。
     * 需要用户已认证。公开程度可能由 Service 层决定。
     *
     * @param userId 要查询的用户 ID。
     * @return 成功时返回 200 OK 和用户的 UserDTO。
     *         如果用户未找到，返回 404 Not Found。
     */
    @Operation(summary = "获取指定用户信息", description = "根据用户 ID 获取用户的公开信息",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "成功获取用户信息", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class)))
    @ApiResponse(responseCode = "401", description = "未认证")
    @ApiResponse(responseCode = "404", description = "指定用户未找到")
    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()") // Or permitAll() if profiles are public
    public ResponseEntity<UserDTO> getUserById(@Parameter(description = "要查询的用户ID") @PathVariable String userId) {
        // Let service handle potential privacy/access control if needed
        UserDTO userDto = userService.getUserById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + userId));
        return ResponseEntity.ok(userDto);
    }

    /**
     * 更新指定用户的信息 (例如: email, avatarUrl)。
     * 仅允许用户本人或管理员操作。
     *
     * @param userId 要更新的用户 ID。
     * @param profileUpdateDTO 包含要更新的字段的 UserProfileUpdateDTO。
     * @return 成功时返回 200 OK 和更新后的 UserDTO。
     *         如果权限不足，返回 403 Forbidden。
     *         如果用户未找到，返回 404 Not Found。
     *         如果输入无效（例如邮箱格式错误或已存在），返回 400 Bad Request。
     */
    @Operation(summary = "更新用户信息", description = "更新指定用户的个人信息（如邮箱、头像），仅限用户本人或管理员。",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class)))
    @ApiResponse(responseCode = "400", description = "请求无效（例如邮箱已存在）")
    @ApiResponse(responseCode = "401", description = "未认证")
    @ApiResponse(responseCode = "403", description = "无权限更新此用户信息")
    @ApiResponse(responseCode = "404", description = "要更新的用户未找到")
    @PutMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> updateUserProfile(
            @Parameter(description = "要更新的用户ID") @PathVariable String userId,
            @Valid @RequestBody UserProfileUpdateDTO profileUpdateDTO) {

        String currentUserId = SecurityUtils.getCurrentUserIdOrThrow();

        boolean isAdmin = SecurityUtils.getCurrentUserDetails()
                .map(ud -> ud.getAuthorities().stream().anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority())))
                .orElse(false);

        // 权限检查：非管理员且非本人，则抛出 AccessDeniedException
        if (!isAdmin && !currentUserId.equals(userId)) {
            log.warn("Permission Denied: User '{}' attempted to update profile for user '{}'.", currentUserId, userId);
            throw new AccessDeniedException("You do not have permission to update this user profile."); // Let GlobalExceptionHandler handle this
        }

        // Call the service method (which now expects UserProfileUpdateDTO)
        UserDTO updatedUser = userService.updateUserProfile(userId, profileUpdateDTO);
        return ResponseEntity.ok(updatedUser);
    }

    // --- Potential Admin Endpoints ---
    /**
     * 获取所有用户列表（分页，仅限管理员）。
     */
    @Operation(summary = "获取所有用户列表（分页）", description = "获取系统内所有用户的信息，仅限管理员访问。",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "成功获取用户列表")
    @ApiResponse(responseCode = "401", description = "未认证")
    @ApiResponse(responseCode = "403", description = "无权限访问")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageDTO<UserDTO>> getAllUsers(
            @Parameter(description = "页码 (从1开始)") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        // TODO: 实现 userService.getAllUsers(pageNum, pageSize) 方法
        log.info("Admin request to get all users, pageNum={}, pageSize={}", pageNum, pageSize);
        // PageDTO<UserDTO> users = userService.getAllUsers(pageNum, pageSize);
        // return ResponseEntity.ok(users);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build(); // Placeholder
    }

    /**
     * 删除指定用户（仅限管理员）。
     */
    @Operation(summary = "删除用户", description = "根据用户 ID 删除指定用户，仅限管理员操作。",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "删除成功")
    @ApiResponse(responseCode = "401", description = "未认证")
    @ApiResponse(responseCode = "403", description = "无权限操作")
    @ApiResponse(responseCode = "404", description = "要删除的用户未找到")
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@Parameter(description = "要删除的用户ID") @PathVariable String userId) {
         // TODO: 实现 userService.deleteUser(userId) 方法，处理关联数据或抛出异常
        log.warn("Admin request to delete user: {}", userId); // Log admin action
        // userService.deleteUser(userId);
        // return ResponseEntity.noContent().build();
         return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build(); // Placeholder
    }
} 