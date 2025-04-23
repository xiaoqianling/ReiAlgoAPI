package com.rei.algo.security;

import com.rei.algo.model.entity.User; // Your User entity that implements UserDetails
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component; // Mark as component if needed for injection, or keep static

import java.util.Optional;

/**
 * 安全相关的工具类
 */
public final class SecurityUtils { // Make final, private constructor for utility class

    private SecurityUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * 获取当前登录用户的 Authentication 对象
     * @return Authentication 对象，如果未认证则返回 null
     */
    public static Optional<Authentication> getCurrentAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * 获取当前登录用户的 UserDetails 对象
     * @return UserDetails 对象 Optional，如果未认证或 Principal 不是 UserDetails 类型则为空
     */
    public static Optional<UserDetails> getCurrentUserDetails() {
        return getCurrentAuthentication()
                .map(Authentication::getPrincipal)
                .filter(UserDetails.class::isInstance)
                .map(UserDetails.class::cast);
    }

    /**
     * 获取当前登录用户的用户名
     * @return 用户名 Optional，如果未认证则为空
     */
    public static Optional<String> getCurrentUsername() {
        return getCurrentUserDetails().map(UserDetails::getUsername);
    }

    /**
     * 获取当前登录用户的 User ID (假设 Principal 是 User 实体类型)
     * @return 用户 ID Optional，如果未认证或 Principal 不是 User 类型则为空
     */
    public static Optional<String> getCurrentUserId() {
        return getCurrentAuthentication()
                .map(Authentication::getPrincipal)
                .filter(User.class::isInstance) // Check if principal is your User entity
                .map(User.class::cast)
                .map(User::getUserId); // Call getUserId() on your User entity
    }

     /**
     * 获取当前登录用户的 User ID，如果未找到则抛出异常
     * @return 用户 ID
     * @throws IllegalStateException 如果用户未认证或无法获取 User ID
     */
    public static String getCurrentUserIdOrThrow() {
        return getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Could not retrieve current user ID from Security Context"));
    }

    /**
     * 检查当前用户是否已认证
     * @return 如果已认证且不是匿名用户则返回 true
     */
    public static boolean isAuthenticated() {
        return getCurrentAuthentication()
                .map(auth -> auth.isAuthenticated() && !isAnonymous(auth))
                .orElse(false);
    }

    private static boolean isAnonymous(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof String) {
            return "anonymousUser".equals(principal);
        }
        return false;
    }
} 