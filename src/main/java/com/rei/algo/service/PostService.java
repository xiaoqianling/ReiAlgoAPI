package com.rei.algo.service;

import com.rei.algo.DTO.PageDTO;
import com.rei.algo.DTO.post.PostCreateRequestDTO;
import com.rei.algo.DTO.post.PostDTO;
import com.rei.algo.DTO.post.PostUpdateRequestDTO;
import com.rei.algo.DTO.post.PostSummaryDTO;
import com.rei.algo.model.enums.EvaluationType;

import java.util.Optional;

/**
 * 帖子服务接口
 */
public interface PostService {

    /**
     * 创建新帖子
     * @param postDTO 包含帖子信息 (title, content, tagNames) 的 DTO
     * @param creatorUserId 创建者用户 ID
     * @return 创建的帖子 DTO (包含详细信息)
     */
    PostDTO createPost(PostCreateRequestDTO postDTO, String creatorUserId);

    /**
     * 更新帖子信息
     * @param postId 要更新的帖子 ID
     * @param postDTO 包含更新信息 (title, content, tagNames) 的 DTO
     * @param currentUserId 当前用户 ID (用于权限检查)
     * @return 更新后的帖子 DTO (包含详细信息)
     * @throws RuntimeException 如果帖子不存在或用户无权更新
     */
    PostDTO updatePost(String postId, PostUpdateRequestDTO postDTO, String currentUserId);

    /**
     * 删除帖子
     * @param postId 要删除的帖子 ID
     * @param currentUserId 当前用户 ID (用于权限检查)
     * @throws RuntimeException 如果帖子不存在或用户无权删除
     */
    void deletePost(String postId, String currentUserId);

    /**
     * 根据 ID 获取帖子详情 (包含作者和标签)
     * @param postId 帖子 ID
     * @return 帖子 DTO (Optional)
     */
    Optional<PostDTO> getPostById(String postId);

    /**
     * 获取指定用户发布的帖子梗概列表 (分页)
     * @param userId 用户 ID
     * @param pageNum 页码 (从 1 开始)
     * @param pageSize 每页数量
     * @return 分页后的帖子梗概 DTO 列表
     */
    PageDTO<PostSummaryDTO> getPostsByUserId(String userId, int pageNum, int pageSize);

    /**
     * 搜索帖子梗概 (标题或内容关键字，分页)
     * @param keyword 关键字 (可能为空)
     * @param pageNum 页码 (从 1 开始)
     * @param pageSize 每页数量
     * @return 分页后的帖子梗概 DTO 列表
     */
    PageDTO<PostSummaryDTO> searchPosts(String keyword, int pageNum, int pageSize);

     /**
     * 获取所有帖子梗概列表 (分页)
     * @param pageNum 页码 (从 1 开始)
     * @param pageSize 每页数量
     * @return 分页后的帖子梗概 DTO 列表
     */
    PageDTO<PostSummaryDTO> listAllPosts(int pageNum, int pageSize);

    /**
     * 获取帖子梗概列表（分页）。
     * @param pageNum 页码 (从1开始)。
     * @param pageSize 每页数量。
     * @return 帖子梗概分页 DTO。
     */
    PageDTO<PostSummaryDTO> getPostSummaries(int pageNum, int pageSize);

    /**
     * 增加指定帖子的浏览量。
     * @param postId 帖子 ID。
     */
    void incrementView(String postId);

    /**
     * 用户对帖子进行评价（点赞/点踩）。
     * @param postId 帖子 ID。
     * @param userId 进行评价的用户 ID。
     * @param evaluationType 评价类型 (LIKE/DISLIKE)。
     * @throws com.rei.algo.exception.ResourceNotFoundException 如果帖子未找到。
     * @throws IllegalStateException 如果评价类型无效。
     */
    void evaluatePost(String postId, String userId, EvaluationType evaluationType);

    // TODO: 添加根据标签查询帖子的方法
    // PageDTO<PostDTO> getPostsByTag(String tagId, int pageNum, int pageSize);
} 