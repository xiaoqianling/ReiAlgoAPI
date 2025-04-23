package com.rei.algo.mapper;

import com.rei.algo.model.entity.Algorithm;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface AlgorithmMapper {

    /**
     * 插入新算法
     * @param algorithm 算法实体
     * @return 影响行数
     */
    int insert(Algorithm algorithm);

    /**
     * 更新算法信息 (动态更新)
     * @param algorithm 算法实体
     * @return 影响行数
     */
    int update(Algorithm algorithm);

    /**
     * 根据 ID 删除算法
     * @param algoId 算法 ID
     * @return 影响行数
     */
    int deleteById(@Param("algoId") String algoId);

    /**
     * 根据 ID 查询算法
     * @param algoId 算法 ID
     * @return 算法信息 (Optional)
     */
    Optional<Algorithm> findById(@Param("algoId") String algoId);

    /**
     * 根据用户 ID 查询该用户创建的算法列表 (可分页)
     * @param userId 用户 ID
     * @param offset 偏移量 (用于分页)
     * @param limit 数量 (用于分页)
     * @return 算法列表
     */
    List<Algorithm> findByUserId(@Param("userId") String userId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 计算用户创建的算法总数
     * @param userId 用户 ID
     * @return 总数
     */
    long countByUserId(@Param("userId") String userId);


    /**
     * 根据关键字搜索公开算法 (标题或描述，可分页)
     * @param keyword 关键字
     * @param offset 偏移量
     * @param limit 数量
     * @return 算法列表
     */
    List<Algorithm> searchPublic(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);

     /**
     * 计算关键字搜索公开算法的总数
     * @param keyword 关键字
     * @return 总数
     */
    long countPublicByKeyword(@Param("keyword") String keyword);

     /**
     * 查询所有公开算法 (可分页)
     * @param offset 偏移量
     * @param limit 数量
     * @return 算法列表
     */
    List<Algorithm> findAllPublic(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 计算所有公开算法的总数
     * @return 总数
     */
    long countAllPublic();

    // 可以添加根据用户ID和关键字搜索等组合查询
} 