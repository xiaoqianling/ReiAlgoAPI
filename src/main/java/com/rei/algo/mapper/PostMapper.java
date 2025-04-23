package com.rei.algo.mapper;

import com.rei.algo.model.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mapper
public interface PostMapper {

    /**
     * 插入新帖子
     * @param post 帖子实体
     * @return 影响行数
     */
    int insert(Post post);

    /**
     * 更新帖子信息 (动态更新)
     * @param post 帖子实体
     * @return 影响行数
     */
    int update(Post post);

    /**
     * 根据 ID 删除帖子
     * @param postId 帖子 ID
     * @return 影响行数
     */
    int deleteById(@Param("postId") String postId);

    /**
     * 根据 ID 查询帖子 (可能需要关联查询用户信息和标签)
     * @param postId 帖子 ID
     * @return 帖子信息 (Optional)，包含作者和标签
     */
    Optional<Post> findByIdWithDetails(@Param("postId") String postId);

     /**
     * 根据 ID 查询帖子 (仅帖子基本信息)
     * @param postId 帖子 ID
     * @return 帖子信息 (Optional)
     */
    Optional<Post> findById(@Param("postId") String postId);


    /**
     * 根据用户 ID 查询该用户发布的帖子列表 (可分页)
     * @param userId 用户 ID
     * @param offset 偏移量
     * @param limit 数量
     * @return 帖子列表 (可能需要包含作者信息)
     */
    List<Post> findByUserId(@Param("userId") String userId, @Param("offset") int offset, @Param("limit") int limit);

     /**
     * 计算用户发布的帖子总数
     * @param userId 用户 ID
     * @return 总数
     */
    long countByUserId(@Param("userId") String userId);

    /**
     * 根据关键字搜索帖子 (标题或内容，可分页)
     * @param keyword 关键字
     * @param offset 偏移量
     * @param limit 数量
     * @return 帖子列表 (可能需要包含作者信息)
     */
    List<Post> search(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);

     /**
     * 计算关键字搜索帖子的总数
     * @param keyword 关键字
     * @return 总数
     */
    long countByKeyword(@Param("keyword") String keyword);


    /**
     * 查询所有帖子 (可分页)
     * @param offset 偏移量
     * @param limit 数量
     * @return 帖子列表 (可能需要包含作者信息)
     */
    List<Post> findAll(@Param("offset") int offset, @Param("limit") int limit);

     /**
     * 计算所有帖子的总数
     * @return 总数
     */
    long countAll();


    // --- Post-Tag Relationship --- //

    /**
     * 批量插入帖子与标签的关联关系 (通常在 XML 中使用 foreach)
     * @param postId 帖子 ID
     * @param tagIds 标签 ID 集合
     * @return 影响行数
     */
    int addTagsToPost(@Param("postId") String postId, @Param("tagIds") Set<String> tagIds);

    /**
     * 删除指定帖子的所有标签关联
     * @param postId 帖子 ID
     * @return 影响行数
     */
    int removeAllTagsFromPost(@Param("postId") String postId);

     /**
     * 删除帖子与指定标签的关联关系
     * @param postId 帖子 ID
     * @param tagId 标签 ID
     * @return 影响行数
     */
    int removeTagFromPost(@Param("postId") String postId, @Param("tagId") String tagId);

} 