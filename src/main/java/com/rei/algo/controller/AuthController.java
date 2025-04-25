package com.rei.algo.controller;

import com.rei.algo.DTO.auth.LoginRequestDTO;
import com.rei.algo.DTO.auth.LoginResponseDTO;
import com.rei.algo.DTO.user.UserDTO;
import com.rei.algo.DTO.auth.RegisterRequestDTO;
import com.rei.algo.security.JwtTokenProvider;
import com.rei.algo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;

@Tag(name = "Authentication", description = "用户认证和注册相关 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    /**
     * 用户注册接口。
     * 接收用户信息（用户名、密码、邮箱等），创建新用户。
     *
     * @param registerRequest 包含注册信息的 RegisterRequestDTO 对象。
     * @return 注册成功时返回 201 Created 和新创建的用户信息 (不含密码)。
     *         如果用户名或邮箱已存在，返回 400 Bad Request。
     *         其他错误返回 500 Internal Server Error。
     */
    @Operation(summary = "用户注册", description = "使用用户名、密码和邮箱注册新用户")
    @ApiResponse(responseCode = "201", description = "注册成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class)))
    @ApiResponse(responseCode = "400", description = "注册失败（例如用户名/邮箱已存在，或输入无效）")
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        log.info("Received registration request for username: {}", registerRequest.getUsername());
        try {
            // Let GlobalExceptionHandler handle IllegalArgumentException and other exceptions
            UserDTO registeredUser = userService.registerUser(registerRequest);
            log.info("User registered successfully: {}", registeredUser.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
        } catch (IllegalArgumentException e) {
            log.warn("Registration failed for username {}: {}", registerRequest.getUsername(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during registration for username {}: {}", registerRequest.getUsername(), e.getMessage(), e);
            throw e;
        }
    }


    /**
     * 用户登录接口。
     * 使用用户名和密码进行认证，成功后返回 JWT 访问令牌。
     *
     * @param loginRequest 包含用户名和密码的 LoginRequestDTO 对象。
     * @return 登录成功时返回 200 OK 和包含 accessToken 的 LoginResponseDTO。
     *         如果认证失败（用户名或密码错误），返回 401 Unauthorized。
     */
    @Operation(summary = "用户登录", description = "使用用户名和密码获取 JWT 访问令牌")
    @ApiResponse(responseCode = "200", description = "登录成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "认证失败（用户名或密码错误）")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("Received login request for username: {}", loginRequest.getUsername());
        try {
            // AuthenticationException will be caught by GlobalExceptionHandler
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);

            // Log successful login - consider logging user ID if easily available in 'authentication' principal
            String username = loginRequest.getUsername();
            if (authentication.getPrincipal() instanceof UserDetails) {
                username = ((UserDetails) authentication.getPrincipal()).getUsername();
            }
            log.info("User '{}' authenticated successfully.", username);

            return ResponseEntity.ok(new LoginResponseDTO(jwt));
        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user {}: {}", loginRequest.getUsername(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during authentication for user {}: {}", loginRequest.getUsername(), e.getMessage(), e);
            throw e;
        }
    }

    // Optional: /logout endpoint (if using stateful tokens or server-side session management)
    // Optional: /refresh token endpoint (if using refresh tokens)
} 