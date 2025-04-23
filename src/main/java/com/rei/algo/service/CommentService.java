package com.rei.algo.service;

import com.rei.algo.DTO.CommentDTO;
import com.rei.algo.DTO.PageDTO;

import java.util.Optional;

/**
 * 评论服务接口
 */
public interface CommentService {

    /**
     * 创建新评论
     * @param commentDTO 包含评论信息 (postId, content, parentCommentId[可选]) 的 DTO
     * @param creatorUserId 创建者用户 ID
     * @return 创建的评论 DTO (包含作者信息)
     * @throws RuntimeException 如果帖子不存在或父评论不存在 (如果 parentCommentId 提供)
     */
    CommentDTO createComment(CommentDTO commentDTO, String creatorUserId);

    /**
     * 更新评论内容 (通常非常受限，例如只允许短时间内修改)
     * @param commentId 要更新的评论 ID
     * @param content 新的评论内容
     * @param currentUserId 当前用户 ID (用于权限检查)
     * @return 更新后的评论 DTO (包含作者信息)
     * @throws RuntimeException 如果评论不存在或用户无权更新
     */
    CommentDTO updateCommentContent(String commentId, String content, String currentUserId);


    /**
     * 删除评论
     * @param commentId 要删除的评论 ID
     * @param currentUserId 当前用户 ID (用于权限检查)
     * @throws RuntimeException 如果评论不存在或用户无权删除
     */
    void deleteComment(String commentId, String currentUserId);

    /**
     * 根据帖子 ID 获取评论列表 (分页，包含作者信息和嵌套回复)
     * 实现时需要处理嵌套加载逻辑。
     * @param postId 帖子 ID
     * @param pageNum 页码 (针对顶级评论)
     * @param pageSize 每页数量 (针对顶级评论)
     * @return 分页后的顶级评论 DTO 列表 (每个 DTO 可能包含其下所有回复)
     */
    PageDTO<CommentDTO> getCommentsByPostId(String postId, int pageNum, int pageSize);

    /**
     * 根据用户 ID 获取该用户发表的评论列表 (分页)
     * @param userId 用户 ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页后的评论 DTO 列表 (包含作者信息) // 可能需要包含 Post 摘要
     */
    PageDTO<CommentDTO> getCommentsByUserId(String userId, int pageNum, int pageSize);

     /**
     * 根据评论ID获取单条评论及其回复(如果需要单独获取)
     * @param commentId 评论ID
     * @return 评论 DTO (Optional)，可能包含回复
     */
    Optional<CommentDTO> getCommentWithReplies(String commentId);

} 