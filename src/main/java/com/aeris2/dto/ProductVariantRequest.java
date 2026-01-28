package com.aeris2.dto;

import lombok.Data;

@Data
public class ProductVariantRequest {
    private String color;
    private String size;
    private int stock;
}
