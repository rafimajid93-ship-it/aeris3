package com.aeris2.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReviewRequest {
    private Long productId;

    @Min(1) @Max(5)
    private int rating;

    @NotBlank
    private String comment;
}
