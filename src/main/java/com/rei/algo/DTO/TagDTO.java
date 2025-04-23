package com.rei.algo.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签数据传输对象 (DTO)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TagDTO {

    private String tagId;

    @NotEmpty(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称过长")
    private String name;

    // 可以添加关联的帖子数量等统计信息 (如果需要)
    // private Long postCount;
} 