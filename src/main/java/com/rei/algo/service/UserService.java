package com.rei.algo.service;

import com.rei.algo.model.entity.User;
import com.rei.algo.DTO.UserDTO;
import com.rei.algo.DTO.RegisterRequestDTO;
import com.rei.algo.DTO.UserProfileUpdateDTO;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

/**
 * 用户服务接口，包含用户管理和 Spring Security 的 UserDetailsService 功能
 */
public interface UserService extends UserDetailsService { // 继承 UserDetailsService

    /**
     * 用户注册
     * @param registerRequest DTO 包含用户名、密码、邮箱等
     * @return 创建的用户信息 DTO (不含密码)
     * @throws RuntimeException 如果用户名或邮箱已存在
     */
    UserDTO registerUser(RegisterRequestDTO registerRequest);

    /**
     * 根据用户 ID 获取用户信息
     * @param userId 用户 ID
     * @return 用户信息 DTO (不含密码, Optional)
     */
    Optional<UserDTO> getUserById(String userId);

     /**
     * 根据用户名获取用户实体 (主要用于 UserDetailsService 和内部逻辑)
     * @param username 用户名
     * @return 用户实体 (Optional)
     */
    Optional<User> findUserByUsername(String username);


    /**
     * 更新用户个人资料 (例如头像、邮箱等，通常不允许修改用户名、密码、角色)
     * @param userId 要更新的用户 ID
     * @param updateRequest 包含更新信息的 DTO
     * @return 更新后的用户信息 DTO (不含密码)
     * @throws RuntimeException 如果用户不存在或无权更新 (权限检查应在此方法或调用前完成)
     */
    UserDTO updateUserProfile(String userId, UserProfileUpdateDTO updateRequest);

    // TODO: 添加其他必要的方法，如修改密码、删除用户(管理员)、分页查询用户(管理员)等

} 