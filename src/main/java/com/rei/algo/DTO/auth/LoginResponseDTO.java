package com.rei.algo.DTO.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应 DTO，包含访问令牌
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private String accessToken;
    private String tokenType = "Bearer"; // JWT 令牌类型通常为 Bearer

    // 可以根据需要添加其他信息，例如刷新令牌、用户信息、过期时间等
    // private String refreshToken;
    // private UserDTO user;
    // private Long expiresIn;

    public LoginResponseDTO(String accessToken) {
        this.accessToken = accessToken;
    }
} 