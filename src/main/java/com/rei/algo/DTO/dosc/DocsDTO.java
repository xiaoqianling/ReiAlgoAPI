package com.rei.algo.DTO.dosc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocsDTO {
    private String id;
    private String title;
    private LocalDateTime createdAt;   // DATETIME
    private LocalDateTime updatedAt;   // DATETIME
    private Object content;         // JSON (Mapped as String initially)
}
