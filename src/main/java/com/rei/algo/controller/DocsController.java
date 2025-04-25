package com.rei.algo.controller;

import com.rei.algo.DTO.docs.DocsDTO;
import com.rei.algo.service.DocsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/docs")
@RequiredArgsConstructor
public class DocsController {
    private final DocsService docsService;

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public DocsDTO findById(@PathVariable String id) {
        return docsService.findById(id);
    }
}
