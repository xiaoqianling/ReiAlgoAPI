package com.rei.algo.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tag {

    private String tagId;       // VARCHAR(16)
    private String name;        // VARCHAR(50)

    // Note: Consider adding a List<Post> here if you often need posts associated with a tag
    // private List<Post> posts;
} 