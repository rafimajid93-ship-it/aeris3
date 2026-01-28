package com.aeris2.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class ProductResponse {

    private Long id;

    private String name;
    private String description;

    private BigDecimal price;
    private int stock;

    private String imageUrl;

    private boolean preorder;
    private LocalDate releaseDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long categoryId;
    private String categoryName;

    private Set<String> colors;
    private Set<String> sizes;

    private List<ProductVariantResponse> variants;

    private int reviewCount;
}
