package com.rei.algo.mapper;

import com.rei.algo.DTO.post.PostSummaryDTO;
import com.rei.algo.model.entity.Post;
import com.rei.algo.model.entity.PostEvaluation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

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
     * 根据用户 ID 查询该用户发布的帖子梗概列表 (分页)
     * @param userId 用户 ID
     * @param rowBounds 分页参数
     * @return PostSummaryDTO 列表
     */
    List<PostSummaryDTO> findByUserId(@Param("userId") String userId, RowBounds rowBounds);

     /**
     * 计算用户发布的帖子总数
     * @param userId 用户 ID
     * @return 总数
     */
    long countByUserId(@Param("userId") String userId);

    /**
     * 根据关键字搜索帖子梗概 (标题或内容，可分页)
     * @param keyword 关键字
     * @param rowBounds 分页参数
     * @return PostSummaryDTO 列表
     */
    List<PostSummaryDTO> search(@Param("keyword") String keyword, RowBounds rowBounds);

     /**
     * 计算关键字搜索帖子的总数
     * @param keyword 关键字
     * @return 总数
     */
    long countByKeyword(@Param("keyword") String keyword);


    /**
     * 查询所有帖子梗概 (分页)
     * @param rowBounds 分页参数
     * @return PostSummaryDTO 列表
     */
    List<PostSummaryDTO> findAll(RowBounds rowBounds);

     /**
     * 计算所有帖子的总数 (用于梗概列表的分页)
     * @return 总数
     */
    long countPosts();

    /**
     * 增加指定帖子的浏览量
     *
     * @param postId 帖子 ID
     * @return 影响行数
     */
    int incrementViewCount(@Param("postId") String postId);

    /**
     * 查询用户对特定帖子的评价
     *
     * @param postId 帖子 ID
     * @param userId 用户 ID
     * @return PostEvaluation 实体 (Optional-like, 可能为 null)
     */
    PostEvaluation findEvaluation(@Param("postId") String postId, @Param("userId") String userId);

    /**
     * 插入新的帖子评价
     *
     * @param evaluation 评价实体
     * @return 影响行数
     */
    int insertEvaluation(PostEvaluation evaluation);

    /**
     * 更新已有的帖子评价
     *
     * @param evaluation 评价实体 (必须包含 postId 和 userId)
     * @return 影响行数
     */
    int updateEvaluation(PostEvaluation evaluation);

    // --- Post-Tag Relationship --- //

    /**
     * 批量插入帖子与标签的关联关系
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