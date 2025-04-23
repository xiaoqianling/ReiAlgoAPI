package com.rei.algo.config;

import com.rei.algo.security.JwtAuthenticationFilter; // Import the filter
import com.rei.algo.service.UserService; // Need this for UserDetailsService
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // For disabling CSRF/Session
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Import this filter class
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import jakarta.servlet.http.HttpServletResponse;


@Configuration
@EnableWebSecurity // 启用 Spring Security 的 Web 安全支持
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true) // 启用方法级别的安全注解
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserService userService; // Inject UserService (UserDetailsService)
    private final JwtAuthenticationFilter jwtAuthenticationFilter; // Inject the filter

    // 定义密码编码器 Bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 定义 AuthenticationProvider Bean，使用 UserDetailsService 和 PasswordEncoder
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService); // userService 实现了 UserDetailsService
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // 定义 AuthenticationManager Bean
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    // 配置 SecurityFilterChain (核心安全规则)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF (因为我们使用 JWT，不需要 CSRF 保护)
                .csrf(AbstractHttpConfigurer::disable)

                // 配置 CORS (跨域资源共享) - 使用默认配置或自定义
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfiguration = new org.springframework.web.cors.CorsConfiguration();
                    corsConfiguration.setAllowedOriginPatterns(java.util.Collections.singletonList("*")); // 允许所有来源 (开发环境) - 生产环境应配置具体来源
                    corsConfiguration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    corsConfiguration.setAllowedHeaders(java.util.Collections.singletonList("*"));
                    corsConfiguration.setAllowCredentials(true);
                    return corsConfiguration;
                }))

                // 配置请求授权规则
                .authorizeHttpRequests(authorize -> authorize
                        // Authentication & API Docs
                        .requestMatchers("/api/auth/**").permitAll().requestMatchers("/swagger-ui/**").permitAll().requestMatchers("/v3/api-docs/**").permitAll()

                        // ----- 公共只读 API (GET) -----
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/tags/**").permitAll() // 允许获取标签
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/posts").permitAll() // 允许获取帖子列表
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/posts/search").permitAll() // 允许搜索帖子
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/posts/user/{userId}").permitAll() // 允许获取用户帖子
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/posts/{postId}").permitAll() // 允许获取单个帖子
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/comments/post/{postId}").permitAll() // 允许获取帖子评论
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/comments/{commentId}/with-replies").permitAll() // 允许获取评论带回复
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/algorithms/public").permitAll() // 允许获取公开算法列表
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/algorithms/search").permitAll() // 允许搜索公开算法
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/algorithms/{algoId}").permitAll() // 允许获取单个算法(Service层处理私有)

                        // 允许 OPTIONS 预检请求
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS).permitAll()

                        // 其他所有请求都需要认证
                        .anyRequest().authenticated())

                // 配置 Session 管理策略为 STATELESS (因为我们使用 JWT)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 设置 AuthenticationProvider
                .authenticationProvider(authenticationProvider())

                // ***** Add the JWT filter before the standard authentication filter *****
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // ***** Explicitly configure exception handling *****
                .exceptionHandling(exceptions -> exceptions
                    .authenticationEntryPoint((request, response, authException) -> {
                        // Handle authentication errors (e.g., invalid token format, signature, expiry)
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"" + authException.getMessage() + "\"}");
                    })
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        // Handle authorization errors (e.g., insufficient permissions)
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"" + accessDeniedException.getMessage() + "\"}");
                    })
                );

        return http.build();
    }
} 