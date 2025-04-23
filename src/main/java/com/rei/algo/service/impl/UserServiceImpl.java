package com.rei.algo.service.impl;

import com.rei.algo.DTO.UserDTO;
import com.rei.algo.DTO.RegisterRequestDTO;
import com.rei.algo.DTO.UserProfileUpdateDTO;
import com.rei.algo.exception.ResourceNotFoundException;
import com.rei.algo.mapper.UserMapper;
import com.rei.algo.model.entity.Role;
import com.rei.algo.model.entity.User;
import com.rei.algo.service.UserService;
import com.rei.algo.util.IDGenerator; // Assuming class name is IDGenerator based on previous edit
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.context.annotation.Lazy;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
//@RequiredArgsConstructor // 由于需要手动添加 @Lazy，移除 Lombok 的构造函数生成
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder; // PasswordEncoder 将被延迟加载

    // 手动添加构造函数并对 PasswordEncoder 使用 @Lazy
    public UserServiceImpl(UserMapper userMapper, @Lazy PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    // --- UserDetailsService Implementation --- //

    /**
     * Locates the user based on the username. Used by Spring Security.
     */
    @Override
    @Transactional(readOnly = true) // Read-only transaction
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userMapper.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    // --- UserService Implementation --- //

    @Override
    @Transactional // Default transaction propagation (REQUIRED)
    public UserDTO registerUser(RegisterRequestDTO registerRequest) {
        // Validation is handled by @Valid in controller

        // Check for existing username
        if (userMapper.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException("用户名已存在: " + registerRequest.getUsername());
        }
        // Check for existing email ONLY IF email is provided
        if (registerRequest.getEmail() != null && !registerRequest.getEmail().trim().isEmpty()) {
             if (userMapper.existsByEmail(registerRequest.getEmail())) {
                throw new IllegalArgumentException("邮箱已存在: " + registerRequest.getEmail());
             }
        }

        // Generate unique User ID
        String userId = IDGenerator.generateUserId();

        User newUser = User.builder()
                .userId(userId)
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword())) // Get password from new DTO
                .email(registerRequest.getEmail()) // Get email from new DTO
                .role(Role.USER) // Always set Role to USER
                .avatarUrl(null) // Always null on registration
                .createdAt(LocalDateTime.now())
                .build();

        userMapper.insert(newUser);

        // Return DTO without password
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

        // 1. Find existing user
        User existingUser = userMapper.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId)); // Use specific exception

        // 2. Security Check is handled in Controller before calling this method

        // 3. Prepare update object (only update allowed fields)
        boolean needsUpdate = false;
        User userToUpdate = User.builder().userId(userId).build(); // Start with just the ID

        // Check and set email if provided and different
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().equals(existingUser.getEmail())) {
            // Check uniqueness before setting
            if (userMapper.existsByEmail(updateRequest.getEmail())) {
                 throw new IllegalArgumentException("邮箱已存在: " + updateRequest.getEmail());
            }
            userToUpdate.setEmail(updateRequest.getEmail());
            needsUpdate = true;
        }

        // Check and set avatarUrl if provided and different
        if (updateRequest.getAvatarUrl() != null && !updateRequest.getAvatarUrl().equals(existingUser.getAvatarUrl())) {
            userToUpdate.setAvatarUrl(updateRequest.getAvatarUrl());
            needsUpdate = true;
        }

        // 4. Perform partial update only if there are changes
        if (needsUpdate) {
             int updatedRows = userMapper.update(userToUpdate); // Assuming update ignores null fields
             if (updatedRows == 0) {
                  // This might happen if the user was deleted concurrently, or if update logic fails
                  throw new RuntimeException("Failed to update user profile for ID: " + userId);
             }
             // Re-fetch the user to get the complete updated state
             existingUser = userMapper.findById(userId).orElseThrow(() -> new RuntimeException("Failed to fetch updated user profile after update for ID: " + userId));

        }
        // If no changes were provided in the DTO, we can just return the existing user data.

        // 5. Return DTO of the (potentially updated) user
        return existingUser.convertToDTO();
    }

     // private User convertToEntity(UserDTO dto) { ... } // Might be needed for updates
} 