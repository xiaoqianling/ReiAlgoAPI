package com.rei.algo.mapper;

import com.rei.algo.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface UserMapper {

    /**
     * 根据用户名查询用户 (用于登录验证)
     * @param username 用户名
     * @return 用户信息 (Optional)
     */
    Optional<User> findByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     * @param email 邮箱
     * @return 用户信息 (Optional)
     */
    Optional<User> findByEmail(@Param("email") String email);

    /**
     * 根据用户ID查询用户
     * @param userId 用户ID
     * @return 用户信息 (Optional)
     */
    Optional<User> findById(@Param("userId") String userId);

    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(@Param("username") String username);

    /**
     * 检查邮箱是否存在
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(@Param("email") String email);

    /**
     * 检查用户ID是否存在
     * @param userId 用户ID
     * @return 是否存在
     */
    boolean existsById(@Param("userId") String userId);

    /**
     * 插入新用户
     * @param user 用户实体
     * @return 影响行数
     */
    int insert(User user);

    /**
     * 更新用户信息 (通常在 XML 中实现动态更新非空字段)
     * @param user 用户实体
     * @return 影响行数
     */
    int update(User user);

    /**
     * 根据用户ID删除用户
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteById(@Param("userId") String userId);

    // 可以根据需要添加其他查询方法，例如分页查询所有用户等
} 