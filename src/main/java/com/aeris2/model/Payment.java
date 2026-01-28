package com.aeris2.model;

import com.aeris2.model.enums.PaymentMethod;
import com.aeris2.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;  // BKASH, CARD, COD

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;  // PENDING, SUCCESS, FAILED

    private BigDecimal amount;

    private String transactionId; // from gateway or internal code

    private Instant createdAt = Instant.now();

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;
}
