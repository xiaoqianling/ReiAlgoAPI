package com.rei.algo.service;

import com.rei.algo.DTO.docs.DocsDTO;
import org.apache.ibatis.annotations.Param;

public interface DocsService {
    DocsDTO findById(@Param("id") String id);
}
