package com.rei.algo.model.entity;

import com.rei.algo.DTO.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    private String userId;        // VARCHAR(8)
    private String username;      // VARCHAR(50)
    private String password;      // VARCHAR(255) - Mapped to UserDetails password
    private String email;         // VARCHAR(100)
    private Role role;            // ENUM('USER', 'ADMIN')
    private String avatarUrl;     // VARCHAR(255)
    private LocalDateTime createdAt; // DATETIME

    // --- UserDetails Methods --- //

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 返回基于 'ROLE_' + role 枚举名称的权限
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public String getPassword() {
        // UserDetails 需要的是密码字段
        return this.password;
    }

    @Override
    public String getUsername() {
        // UserDetails 使用 username 字段
        return this.username;
    }

    // 以下方法可以根据需要实现更复杂的逻辑 (例如账户锁定、过期等)
    @Override
    public boolean isAccountNonExpired() {
        return true; // 账户未过期
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 账户未锁定
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 凭证未过期
    }

    @Override
    public boolean isEnabled() {
        return true; // 账户已启用
    }

    public UserDTO convertToDTO() {
        return UserDTO.builder()
                .userId(this.userId)
                .username(this.username)
                .email(this.email)
                .role(this.role)
                .avatarUrl(this.avatarUrl)
                .createdAt(this.createdAt)
                // DO NOT include passwordHash in DTO
                .build();
    }
} 