package com.aeris2.controller;
//
import com.aeris2.dto.OrderResponse;
import com.aeris2.model.Order;
import com.aeris2.model.enums.OrderStatus;
import com.aeris2.repository.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/orders")
@CrossOrigin
public class AdminOrderController {

    private final OrderRepository orderRepo;

    public AdminOrderController(OrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    // ✅ List all orders
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<Order> list = orderRepo.findAll();
        return ResponseEntity.ok(
                list.stream().map(this::toResponse).collect(Collectors.toList())
        );
    }

    // ✅ Get one order
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return orderRepo.findById(id)
                .map(o -> ResponseEntity.ok(toResponse(o)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ FIXED: Update order status (accepts { "status": "PROCESSING" })
    @PatchMapping("/{id}/status")
    @Transactional
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String statusValue = body.get("status");
        if (statusValue == null) {
            return ResponseEntity.badRequest().build();
        }

        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        try {
            OrderStatus newStatus = OrderStatus.valueOf(statusValue.toUpperCase());
            order.setStatus(newStatus);
            orderRepo.save(order);
            return ResponseEntity.ok(toResponse(order));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ✅ Delete order
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        if (!orderRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        orderRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ Reuse same DTO mapping logic
//    private OrderResponse toResponse(Order order) {
//        OrderResponse res = new OrderResponse();
//        res.setId(order.getId());
//        res.setUserName(order.getUser().getName());
//        res.setUserPhone(order.getPhoneNumber());
//        res.setTotalAmount(order.getTotalAmount());
//        res.setShippingAddress(order.getShippingAddress());
//        res.setStatus(order.getStatus());
//        res.setCreatedAt(order.getCreatedAt());
//
//        if (order.getPayment() != null) {
//            res.setPaymentMethod(order.getPayment().getMethod());
//            res.setPaymentStatus(order.getPayment().getStatus());
//        }
//
//        res.setItems(order.getItems().stream().map(i -> {
//            var dto = new com.aeris2.dto.OrderItemResponse();
//            dto.setProductId(i.getProduct().getId());
//            dto.setProductName(i.getProduct().getName());
//            dto.setPrice(i.getPrice());
//            dto.setQuantity(i.getQuantity());
//            dto.setColor(i.getColor());
//            dto.setSize(i.getSize());
//            dto.setPreorder(i.getProduct().isPreorder());
//            return dto;
//        }).collect(Collectors.toList()));
//
//        return res;
//    }
    private OrderResponse toResponse(Order order) {
        OrderResponse res = new OrderResponse();
        res.setId(order.getId());
        res.setUserName(order.getUser().getName());
        res.setUserPhone(order.getPhoneNumber());

        // ✅ NEW
        res.setName(order.getName());
        res.setFacebookId(order.getFacebookId());

        res.setTotalAmount(order.getTotalAmount());
        res.setShippingAddress(order.getShippingAddress());
        res.setStatus(order.getStatus());
        res.setCreatedAt(order.getCreatedAt());

        if (order.getPayment() != null) {
            res.setPaymentMethod(order.getPayment().getMethod());
            res.setPaymentStatus(order.getPayment().getStatus());
        }

        res.setItems(order.getItems().stream().map(i -> {
            var dto = new com.aeris2.dto.OrderItemResponse();
            dto.setProductId(i.getProduct().getId());
            dto.setProductName(i.getProduct().getName());
            dto.setPrice(i.getPrice());
            dto.setQuantity(i.getQuantity());
            dto.setColor(i.getColor());
            dto.setSize(i.getSize());
            dto.setPreorder(i.getProduct().isPreorder());
            return dto;
        }).collect(Collectors.toList()));

        return res;
    }

}
