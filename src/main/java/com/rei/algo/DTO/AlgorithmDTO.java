package com.rei.algo.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 算法数据传输对象 (DTO)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlgorithmDTO {

    private String algoId;

    // 在创建时不传入，由后端根据当前用户设置
    private String userId;
    private UserDTO user; // 作者信息 (可选，用于返回)

    @NotEmpty(message = "算法标题不能为空")
    @Size(max = 255, message = "标题过长")
    private String title;

    private String description;

    @NotEmpty(message = "代码内容不能为空")
    private String codeContent;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @NotNull(message = "请指定算法是否公开")
    private Boolean isPublic;
} 