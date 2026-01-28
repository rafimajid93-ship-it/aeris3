package com.aeris2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    @Size(max = 1000, message = "Description can be at most 1000 characters")
    private String description;
}
