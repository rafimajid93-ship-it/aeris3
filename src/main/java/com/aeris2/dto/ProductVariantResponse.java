package com.aeris2.dto;

import lombok.Data;

@Data
public class ProductVariantResponse {
    private String color;
    private String size;
    private int stock;
}
