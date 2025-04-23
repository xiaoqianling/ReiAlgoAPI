package com.rei.algo.security;

import com.rei.algo.service.UserService; // To load UserDetails by username
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component // Mark as a Spring component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter { // Ensures filter runs once per request

    private final JwtTokenProvider tokenProvider;
    private final UserService userService; // UserDetailsService implementation

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1. 从请求中获取 JWT
            String jwt = getJwtFromRequest(request);

            // 2. 验证 Token 并加载用户信息
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String username = tokenProvider.getUsernameFromJWT(jwt);

                // 从数据库加载 UserDetails (通过 UserService)
                // Check if authentication already exists in context (might happen in some scenarios)
                 if (SecurityContextHolder.getContext().getAuthentication() == null) {
                     UserDetails userDetails = userService.loadUserByUsername(username);

                     // 创建 Authentication 对象
                     UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                             userDetails, // Principal (可以是 UserDetails 或自定义对象)
                             null,        // Credentials (not needed for JWT)
                             userDetails.getAuthorities()); // Authorities (roles/permissions)

                     authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                     // 3. 将 Authentication 对象设置到 SecurityContext 中
                     SecurityContextHolder.getContext().setAuthentication(authentication);
                     log.debug("Authenticated user '{}' set in Security Context", username);
                 } else {
                     log.debug("SecurityContextHolder already contains authentication for '{}'", username);
                 }
            } else {
                 // Only log if the URI is likely expected to have a token (avoid spamming for public endpoints)
                 // Example: if (!request.getRequestURI().startsWith("/api/auth/")) {
                 //     log.debug("No valid JWT token found in request for {}", request.getRequestURI());
                 // }
            }
        } catch (Exception ex) {
            // 处理过滤器中的异常，例如 Token 解析失败或用户未找到
            log.error("Could not set user authentication in security context for request {}: {}", request.getRequestURI(), ex.getMessage());
            // Ensure context is clean in case of error during auth setup
             SecurityContextHolder.clearContext();
        }

        // 4. 继续执行过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从 HttpServletRequest 的 Authorization Header 中提取 JWT 令牌。
     * @param request 请求对象
     * @return JWT 令牌字符串，如果不存在或格式不正确则返回 null
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // 检查 Header 是否存在且以 "Bearer " 开头
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // 提取 "Bearer " 之后的部分
            return bearerToken.substring(7);
        }
        return null;
    }
} 