package com.aeris2.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}
