package com.rei.algo.service;

import com.rei.algo.DTO.AlgorithmDTO;
import com.rei.algo.DTO.PageDTO;

import java.util.Optional;

/**
 * 算法服务接口
 */
public interface AlgorithmService {

    /**
     * 创建新算法
     * @param algorithmDTO 包含算法信息的 DTO (不含 userId)
     * @param creatorUserId 创建者用户 ID (通常从 SecurityContext 获取)
     * @return 创建的算法 DTO
     */
    AlgorithmDTO createAlgorithm(AlgorithmDTO algorithmDTO, String creatorUserId);

    /**
     * 更新算法信息
     * @param algoId 要更新的算法 ID
     * @param algorithmDTO 包含更新信息的 DTO
     * @param currentUserId 当前用户 ID (用于权限检查)
     * @return 更新后的算法 DTO
     * @throws RuntimeException 如果算法不存在或用户无权更新
     */
    AlgorithmDTO updateAlgorithm(String algoId, AlgorithmDTO algorithmDTO, String currentUserId);

    /**
     * 删除算法
     * @param algoId 要删除的算法 ID
     * @param currentUserId 当前用户 ID (用于权限检查)
     * @throws RuntimeException 如果算法不存在或用户无权删除
     */
    void deleteAlgorithm(String algoId, String currentUserId);

    /**
     * 根据 ID 获取算法详情
     * @param algoId 算法 ID
     * @param currentUserId 当前用户 ID (可能为 null，用于检查私有算法的访问权限)
     * @return 算法 DTO (Optional)
     * @throws RuntimeException 如果算法为私有且用户无权查看 (或者可以选择返回 Optional.empty())
     */
    Optional<AlgorithmDTO> getAlgorithmById(String algoId, String currentUserId);

    /**
     * 获取指定用户创建的算法列表 (分页)
     * @param userId 用户 ID
     * @param pageNum 页码 (从 1 开始)
     * @param pageSize 每页数量
     * @return 分页后的算法 DTO 列表
     */
    PageDTO<AlgorithmDTO> getAlgorithmsByUserId(String userId, int pageNum, int pageSize);

    /**
     * 搜索公开的算法 (分页)
     * @param keyword 关键字 (可能为空或 null)
     * @param pageNum 页码 (从 1 开始)
     * @param pageSize 每页数量
     * @return 分页后的算法 DTO 列表
     */
    PageDTO<AlgorithmDTO> searchPublicAlgorithms(String keyword, int pageNum, int pageSize);

    /**
     * 获取所有公开的算法 (分页)
     * @param pageNum 页码 (从 1 开始)
     * @param pageSize 每页数量
     * @return 分页后的算法 DTO 列表
     */
    PageDTO<AlgorithmDTO> listPublicAlgorithms(int pageNum, int pageSize);
} 