package com.aeris2.controller;
//
import com.aeris2.dto.*;
import com.aeris2.model.*;
import com.aeris2.model.enums.*;
import com.aeris2.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin
public class PaymentController {

    private final PaymentRepository paymentRepo;
    private final OrderRepository orderRepo;

    public PaymentController(PaymentRepository paymentRepo, OrderRepository orderRepo) {
        this.paymentRepo = paymentRepo;
        this.orderRepo = orderRepo;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> makePayment(@RequestBody PaymentRequest req) {
        Optional<Order> orderOpt = orderRepo.findById(req.getOrderId());
        if (orderOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Order order = orderOpt.get();

        Payment payment = Payment.builder()
                .order(order)
                .amount(req.getAmount())
                .method(req.getMethod())
                .status(PaymentStatus.PENDING)
                .transactionId(UUID.randomUUID().toString())
                .build();

        paymentRepo.save(payment);

        // simulate success for demo
        payment.setStatus(PaymentStatus.SUCCESS);
        paymentRepo.save(payment);

        // link order
        order.setPayment(payment);
        orderRepo.save(order);

        return ResponseEntity.ok(toResponse(payment));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id) {
        return paymentRepo.findById(id)
                .map(p -> ResponseEntity.ok(toResponse(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    private PaymentResponse toResponse(Payment p) {
        PaymentResponse res = new PaymentResponse();
        res.setId(p.getId());
        res.setMethod(p.getMethod());
        res.setStatus(p.getStatus());
        res.setAmount(p.getAmount());
        res.setTransactionId(p.getTransactionId());
        res.setCreatedAt(p.getCreatedAt());
        return res;
    }
}
