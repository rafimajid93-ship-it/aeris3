package com.aeris2.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Data
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    private String name;

    @Size(max = 2000, message = "Description can be at most 2000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    /** Global stock (not used when variants exist and preorder=false). */
    private Integer stock;

    private String imageUrl;

    @NotNull(message = "Category is required")
    private Long categoryId;

    /** Preorder toggle */
    private Boolean preorder = false;

    /** Optional release date */
    private LocalDate releaseDate;

    /**
     * Colors/sizes metadata (normalized to lowercase in controller)
     * Using LinkedHashSet preserves order
     */
    private Set<String> colors = new LinkedHashSet<>();
    private Set<String> sizes = new LinkedHashSet<>();

    /**
     * Variant grid input (color + size + stock)
     * Only used when preorder=false
     * Preorder variants default to stock=0
     */
    private List<ProductVariantRequest> variants;
}
