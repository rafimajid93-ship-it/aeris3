package com.aeris2.dto;

import com.aeris2.model.enums.PaymentMethod;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private String shippingAddress;
    private String phone;
    private PaymentMethod paymentMethod;
    private List<OrderItemRequest> items;
    private String name;
    private String facebookId;
}
