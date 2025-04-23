package com.rei.algo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 通用分页结果 DTO
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageDTO<T> {
    private Integer pageNum;    // 当前页码
    private Integer pageSize;   // 每页数量
    private Long total;         // 总记录数
    private Integer pages;      // 总页数
    private List<T> list;       // 当前页数据列表
} 