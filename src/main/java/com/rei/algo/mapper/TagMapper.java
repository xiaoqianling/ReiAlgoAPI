package com.rei.algo.mapper;

import com.rei.algo.model.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mapper
public interface TagMapper {

    /**
     * 插入新标签
     * @param tag 标签实体
     * @return 影响行数
     */
    int insert(Tag tag);

    /**
     * 根据 ID 删除标签
     * @param tagId 标签 ID
     * @return 影响行数
     */
    int deleteById(@Param("tagId") String tagId);

    /**
     * 根据 ID 查询标签
     * @param tagId 标签 ID
     * @return 标签信息 (Optional)
     */
    Optional<Tag> findById(@Param("tagId") String tagId);

    /**
     * 根据名称查询标签
     * @param name 标签名称
     * @return 标签信息 (Optional)
     */
    Optional<Tag> findByName(@Param("name") String name);

    /**
     * 检查标签名称是否存在
     * @param name 标签名称
     * @return 是否存在
     */
    boolean existsByName(@Param("name") String name);

    /**
     * 查询所有标签
     * @return 标签列表
     */
    List<Tag> findAll();

    /**
     * 根据帖子 ID 查询相关的所有标签
     * @param postId 帖子 ID
     * @return 标签列表
     */
    List<Tag> findTagsByPostId(@Param("postId") String postId);

    /**
     * 批量插入标签 (如果不存在)
     * 通常在 XML 中使用 foreach 实现
     * @param tags 标签列表
     * @return 影响行数
     */
    int insertBatchIfNotExists(List<Tag> tags);

     /**
     * 根据一批 Tag ID 查询标签
     * @param tagIds Tag ID 集合
     * @return 标签列表
     */
    List<Tag> findByIds(@Param("tagIds") Set<String> tagIds);

} 