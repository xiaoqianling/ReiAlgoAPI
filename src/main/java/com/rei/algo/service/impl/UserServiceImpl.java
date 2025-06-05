package com.rei.algo.service.impl;

import com.rei.algo.DTO.user.UserDTO;
import com.rei.algo.DTO.auth.RegisterRequestDTO;
import com.rei.algo.DTO.user.UserProfileUpdateDTO;
import com.rei.algo.exception.ResourceNotFoundException;
import com.rei.algo.mapper.UserMapper;
import com.rei.algo.model.entity.Role;
import com.rei.algo.model.entity.User;
import com.rei.algo.service.UserService;
import com.rei.algo.util.IDGenerator;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.context.annotation.Lazy;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, @Lazy PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);
        return userMapper.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户名或密码错误"));
    }

    @Override
    @Transactional
    public UserDTO registerUser(RegisterRequestDTO registerRequest) {
        log.debug("Registering new user: {}", registerRequest.getUsername());
        
        // 检查用户名是否存在
        if (userMapper.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException("用户名已存在: " + registerRequest.getUsername());
        }
        
        // 检查邮箱是否存在
        if (registerRequest.getEmail() != null && !registerRequest.getEmail().trim().isEmpty()) {
            if (userMapper.existsByEmail(registerRequest.getEmail())) {
                throw new IllegalArgumentException("邮箱已存在: " + registerRequest.getEmail());
            }
        }

        // 生成用户ID
        String userId = IDGenerator.generateUserId();

        // 创建新用户
        User newUser = User.builder()
                .userId(userId)
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .email(registerRequest.getEmail())
                .role(Role.USER)
                .avatarUrl(null)
                .createdAt(LocalDateTime.now())
                .build();

        log.debug("Saving new user to database: {}", newUser.getUsername());
        userMapper.insert(newUser);

        return newUser.convertToDTO();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserById(String userId) {
        return userMapper.findById(userId).map(User::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUserByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    @Transactional
    public UserDTO updateUserProfile(String userId, UserProfileUpdateDTO updateRequest) {
        Assert.notNull(userId, "User ID cannot be null");
        Assert.notNull(updateRequest, "Update request cannot be null");

        // 查找现有用户
        User existingUser = userMapper.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));

        // 准备更新对象
        boolean needsUpdate = false;
        User userToUpdate = User.builder().userId(userId).build();

        // 检查并设置邮箱
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().equals(existingUser.getEmail())) {
            if (userMapper.existsByEmail(updateRequest.getEmail())) {
                throw new IllegalArgumentException("邮箱已存在: " + updateRequest.getEmail());
            }
            userToUpdate.setEmail(updateRequest.getEmail());
            needsUpdate = true;
        }

        // 检查并设置头像URL
        if (updateRequest.getAvatarUrl() != null && !updateRequest.getAvatarUrl().equals(existingUser.getAvatarUrl())) {
            userToUpdate.setAvatarUrl(updateRequest.getAvatarUrl());
            needsUpdate = true;
        }

        // 执行更新
        if (needsUpdate) {
            int updatedRows = userMapper.update(userToUpdate);
            if (updatedRows == 0) {
                throw new RuntimeException("Failed to update user profile for ID: " + userId);
            }
            existingUser = userMapper.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Failed to fetch updated user profile after update for ID: " + userId));
        }

        return existingUser.convertToDTO();
    }
} 