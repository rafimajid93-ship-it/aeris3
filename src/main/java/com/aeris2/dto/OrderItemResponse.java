//package com.aeris2.dto;
//
//import lombok.Data;
//import java.math.BigDecimal;
//
//@Data
//public class OrderItemResponse {
//    private Long productId;
//    private String productName;
//    private String color;
//    private String size;
//    private int quantity;
//    private BigDecimal price;
//    private boolean preorder;
//}

package com.aeris2.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponse {

    /**
     * Unique order item row id
     */
    private Long id;

    /**
     * Actual product id
     */
    private Long productId;

    private String productName;
    private Integer quantity;
    private BigDecimal price;
    private String color;
    private String size;
    private boolean preorder;
}