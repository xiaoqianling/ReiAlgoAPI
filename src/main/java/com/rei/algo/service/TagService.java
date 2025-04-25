package com.rei.algo.service;

import com.rei.algo.model.entity.Tag;

import java.util.List;
import java.util.Set;

/**
 * 标签服务接口
 */
public interface TagService {

    /**
     * 创建新标签 (如果不存在)。
     * 公开接口，返回 DTO。
     * @param Tag 标签 DTO (只需要 name)
     * @return 创建或已存在的标签 DTO
     */
    Tag createTag(Tag Tag);

    /**
     * 根据名称查找或创建标签实体。
     * 内部使用，例如供 PostService 调用，确保标签存在并返回带 ID 的实体。
     *
     * @param tagName 标签名称
     * @return 标签实体 (保证存在且包含 ID)
     */
    Tag findOrCreateTagByName(String tagName);

    /**
     * 根据一批名称查找或创建标签实体。
     * 内部使用。
     *
     * @param tagNames 标签名称集合 (不应为空或包含空字符串)
     * @return 标签实体列表 (ID 已填充)
     */
    List<Tag> findOrCreateTagsByNames(Set<String> tagNames);


    /**
     * 获取所有标签
     * @return 标签 DTO 列表
     */
    List<Tag> getAllTags();

    /**
     * 根据帖子 ID 获取标签列表
     * @param postId 帖子 ID
     * @return 标签 DTO 列表
     */
    List<Tag> getTagsByPostId(String postId);


    // 可选的管理员操作:
    /**
     * 删除标签 (管理员权限)
     * 注意：需要考虑是否解除与帖子的关联，或者依赖数据库外键约束。
     * @param tagId 标签 ID
     */
    // void deleteTag(String tagId);

} 