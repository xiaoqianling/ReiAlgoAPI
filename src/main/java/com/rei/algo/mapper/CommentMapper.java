package com.rei.algo.mapper;

import com.rei.algo.model.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CommentMapper {

    /**
     * 插入新评论
     * @param comment 评论实体
     * @return 影响行数
     */
    int insert(Comment comment);

    /**
     * 更新评论内容 (通常不允许或有限制地允许)
     * @param comment 评论实体 (至少包含 commentId 和 content)
     * @return 影响行数
     */
    int updateContent(Comment comment);

    /**
     * 根据 ID 删除评论
     * @param commentId 评论 ID
     * @return 影响行数
     */
    int deleteById(@Param("commentId") String commentId);

    /**
     * 根据 ID 查询评论 (可能需要关联查询用户信息)
     * @param commentId 评论 ID
     * @return 评论信息 (Optional)
     */
    Optional<Comment> findByIdWithUser(@Param("commentId") String commentId);

    /**
     * 根据帖子 ID 查询顶级评论列表 (parentCommentId 为 NULL) (可分页)
     * @param postId 帖子 ID
     * @param offset 偏移量
     * @param limit 数量
     * @return 顶级评论列表 (包含用户信息)
     */
    List<Comment> findTopLevelByPostId(@Param("postId") String postId, @Param("offset") int offset, @Param("limit") int limit);

     /**
     * 计算帖子的顶级评论总数
     * @param postId 帖子 ID
     * @return 总数
     */
    long countTopLevelByPostId(@Param("postId") String postId);

    /**
     * 根据父评论 ID 查询回复列表 (可分页，或一次性加载所有?)
     * @param parentCommentId 父评论 ID
     * @param offset 偏移量
     * @param limit 数量
     * @return 回复列表 (包含用户信息)
     */
    List<Comment> findRepliesByParentId(@Param("parentCommentId") String parentCommentId, @Param("offset") int offset, @Param("limit") int limit);

     /**
     * 计算父评论的回复总数
     * @param parentCommentId 父评论 ID
     * @return 总数
     */
    long countRepliesByParentId(@Param("parentCommentId") String parentCommentId);


    /**
     * 根据用户 ID 查询该用户发表的评论列表 (可分页)
     * @param userId 用户 ID
     * @param offset 偏移量
     * @param limit 数量
     * @return 评论列表 (包含用户信息和帖子标题等摘要信息可能更好)
     */
    List<Comment> findByUserId(@Param("userId") String userId, @Param("offset") int offset, @Param("limit") int limit);

     /**
     * 计算用户发表的评论总数
     * @param userId 用户 ID
     * @return 总数
     */
    long countByUserId(@Param("userId") String userId);

} 