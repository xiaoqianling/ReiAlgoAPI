package com.rei.algo.mapper;

import com.rei.algo.model.entity.Docs;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DocsMapper {

     /**
     * 根据一批 Tag ID 查询标签
     * @param id Tag ID 集合
     * @return 标签列表
     */
    Docs findById(@Param("id") String id);

} 