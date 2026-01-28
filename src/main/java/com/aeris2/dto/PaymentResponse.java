package com.aeris2.dto;

import com.aeris2.model.enums.PaymentMethod;
import com.aeris2.model.enums.PaymentStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class PaymentResponse {
    private Long id;
    private PaymentMethod method;
    private PaymentStatus status;
    private BigDecimal amount;
    private String transactionId;
    private Instant createdAt;
}
