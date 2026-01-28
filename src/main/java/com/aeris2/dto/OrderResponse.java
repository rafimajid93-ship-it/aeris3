package com.aeris2.dto;

import com.aeris2.model.enums.PaymentMethod;
import com.aeris2.model.enums.PaymentStatus;
import com.aeris2.model.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class OrderResponse {

    private Long id;

    private String userName;
    private String userPhone;

    private BigDecimal totalAmount;
    private String shippingAddress;

    private OrderStatus status;
    private Instant createdAt;

    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;

    private List<OrderItemResponse> items;
}
