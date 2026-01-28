package com.aeris2.dto;

import lombok.Data;

@Data
public class WishlistResponse {
    private Long productId;
    private String productName;
    private String imageUrl;
    private Double price;
}
