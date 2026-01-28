package com.aeris2.dto;

import com.aeris2.model.enums.PaymentMethod;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentRequest {
    private Long orderId;
    private PaymentMethod method;
    private BigDecimal amount;
}
