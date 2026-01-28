package com.aeris2.controller;

import com.aeris2.dto.OrderItemRequest;
import com.aeris2.dto.OrderItemResponse;
import com.aeris2.dto.OrderRequest;
import com.aeris2.dto.OrderResponse;
import com.aeris2.model.*;
import com.aeris2.model.enums.OrderStatus;
import com.aeris2.model.enums.PaymentMethod;
import com.aeris2.model.enums.PaymentStatus;
import com.aeris2.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin
public class OrderController {

    private final OrderRepository orderRepo;
    private final UserRepository userRepo;
    private final ProductRepository productRepo;
    private final PaymentRepository paymentRepo;
    private final ProductVariantRepository variantRepo;

    public OrderController(
            OrderRepository orderRepo,
            UserRepository userRepo,
            ProductRepository productRepo,
            PaymentRepository paymentRepo,
            ProductVariantRepository variantRepo
    ) {
        this.orderRepo = orderRepo;
        this.userRepo = userRepo;
        this.productRepo = productRepo;
        this.paymentRepo = paymentRepo;
        this.variantRepo = variantRepo;
    }

    // -----------------------------------------------------
    // Helpers
    // -----------------------------------------------------
    private String normalizeValue(String raw) {
        if (raw == null) return "Default";
        String v = raw.trim();
        if (v.isEmpty()) return "Default";
        if (v.equalsIgnoreCase("default") || v.equalsIgnoreCase("free")) {
            return "Default";
        }
        return v;
    }

    /**
     * We now treat ANY existing variant row as a "variant product",
     * even if it is just Default/Default. Only when there are NO variants
     * at all do we use product-level stock only.
     */
    private boolean hasVariants(List<ProductVariant> variants) {
        return variants != null && !variants.isEmpty();
    }

    private int safeInt(Integer val) {
        return val == null ? 0 : val;
    }

    // -----------------------------------------------------
    // ✅ Place order and update stock / preorder demand
    // -----------------------------------------------------
    @Transactional
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest req,
                                                    Authentication auth) {
        String email = auth.getName();
        User user = userRepo.findByEmail(email).orElseThrow();

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(req.getShippingAddress());
        order.setPhoneNumber(req.getPhone());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (OrderItemRequest i : req.getItems()) {
            Product product = productRepo.findById(i.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + i.getProductId()));

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(i.getQuantity());
            item.setPrice(product.getPrice());

            List<ProductVariant> variants = variantRepo.findByProductId(product.getId());
            boolean hasVariants = hasVariants(variants);

            if (product.isPreorder()) {
                // -------------------------------
                // PREORDER  ➜ INCREASE COUNTS
                // -------------------------------
                if (!hasVariants) {
                    // simple preorder without any variant rows
                    int current = safeInt(product.getStock());
                    product.setStock(current + i.getQuantity());
                    productRepo.save(product);

                    item.setColor(null);
                    item.setSize(null);
                } else {
                    // preorder WITH variants – increase variant stock
                    String color = normalizeValue(i.getColor());
                    String size  = normalizeValue(i.getSize());

                    ProductVariant variant = variantRepo
                            .findByProductIdAndColorIgnoreCaseAndSizeIgnoreCase(
                                    product.getId(), color, size)
                            .orElseThrow(() -> new RuntimeException(
                                    "Variant not allowed for preorder product: "
                                            + product.getName() + " (" + color + " / " + size + ")"
                            ));

                    int currentVarStock = safeInt(variant.getStock());
                    variant.setStock(currentVarStock + i.getQuantity());
                    variantRepo.save(variant);

                    int totalDemand = variantRepo.findByProductId(product.getId())
                            .stream()
                            .mapToInt(v -> safeInt(v.getStock()))
                            .sum();
                    product.setStock(totalDemand);
                    productRepo.save(product);

                    item.setColor(color.equalsIgnoreCase("Default") ? null : color);
                    item.setSize(size.equalsIgnoreCase("Default") ? null : size);
                }

            } else {
                // -------------------------------
                // NORMAL PRODUCT  ➜ DECREASE STOCK
                // -------------------------------
                if (!hasVariants) {
                    // product-level stock only (no variant rows)
                    int current = safeInt(product.getStock());
                    if (current < i.getQuantity()) {
                        throw new RuntimeException("Not enough stock for product: " + product.getName());
                    }
                    product.setStock(current - i.getQuantity());
                    productRepo.save(product);

                    item.setColor(null);
                    item.setSize(null);
                } else {
                    // variant-based stock – ALSO used for Default/Default variant
                    String color = normalizeValue(i.getColor());
                    String size  = normalizeValue(i.getSize());

                    ProductVariant variant = variantRepo
                            .findByProductIdAndColorIgnoreCaseAndSizeIgnoreCase(
                                    product.getId(), color, size)
                            .orElseThrow(() -> new RuntimeException(
                                    "Variant not found for product: "
                                            + product.getName() + " (" + color + " / " + size + ")"
                            ));

                    int currentVarStock = safeInt(variant.getStock());
                    if (currentVarStock < i.getQuantity()) {
                        throw new RuntimeException("Not enough stock for " +
                                product.getName() + " (" + color + " / " + size + ")");
                    }

                    variant.setStock(currentVarStock - i.getQuantity());
                    variantRepo.save(variant);

                    int totalStock = variantRepo.findByProductId(product.getId())
                            .stream()
                            .mapToInt(v -> safeInt(v.getStock()))
                            .sum();
                    product.setStock(totalStock);
                    productRepo.save(product);

                    item.setColor(color.equalsIgnoreCase("Default") ? null : color);
                    item.setSize(size.equalsIgnoreCase("Default") ? null : size);
                }
            }

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())));
            items.add(item);
        }

        order.setItems(items);
        order.setTotalAmount(total);
        orderRepo.save(order);

        // Payment record
        Payment payment = Payment.builder()
                .order(order)
                .amount(total)
                .method(req.getPaymentMethod())
                .status(req.getPaymentMethod() == PaymentMethod.COD
                        ? PaymentStatus.SUCCESS
                        : PaymentStatus.PENDING)
                .transactionId("TXN-" + System.currentTimeMillis())
                .build();

        paymentRepo.save(payment);
        order.setPayment(payment);
        orderRepo.save(order);

        return ResponseEntity.ok(toResponse(order));
    }

    // -----------------------------------------------------
    // Cancel order (reverse stock / preorder demand)
    // -----------------------------------------------------
    @PatchMapping("/{id}/cancel")
    @Transactional
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id) {
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            return ResponseEntity.badRequest().build();
        }

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            int qty = item.getQuantity();

            List<ProductVariant> variants = variantRepo.findByProductId(product.getId());
            boolean hasVariants = hasVariants(variants);

            if (product.isPreorder()) {
                // --------------------- PREORDER: decrease demand ---------------------
                if (!hasVariants) {
                    int current = safeInt(product.getStock());
                    product.setStock(Math.max(0, current - qty));
                    productRepo.save(product);
                } else {
                    String color = normalizeValue(item.getColor());
                    String size  = normalizeValue(item.getSize());

                    ProductVariant variant = variantRepo
                            .findByProductIdAndColorIgnoreCaseAndSizeIgnoreCase(
                                    product.getId(), color, size)
                            .orElse(null);

                    if (variant != null) {
                        int current = safeInt(variant.getStock());
                        variant.setStock(Math.max(0, current - qty));
                        variantRepo.save(variant);
                    }

                    int totalDemand = variantRepo.findByProductId(product.getId())
                            .stream()
                            .mapToInt(v -> safeInt(v.getStock()))
                            .sum();
                    product.setStock(totalDemand);
                    productRepo.save(product);
                }

            } else {
                // --------------------- NORMAL PRODUCT: restore stock -----------------
                if (!hasVariants) {
                    int current = safeInt(product.getStock());
                    product.setStock(current + qty);
                    productRepo.save(product);
                } else {
                    String color = normalizeValue(item.getColor());
                    String size  = normalizeValue(item.getSize());

                    ProductVariant variant = variantRepo
                            .findByProductIdAndColorIgnoreCaseAndSizeIgnoreCase(
                                    product.getId(), color, size)
                            .orElse(null);

                    if (variant != null) {
                        int current = safeInt(variant.getStock());
                        variant.setStock(current + qty);
                        variantRepo.save(variant);

                        int totalStock = variantRepo.findByProductId(product.getId())
                                .stream()
                                .mapToInt(v -> safeInt(v.getStock()))
                                .sum();
                        product.setStock(totalStock);
                        productRepo.save(product);
                    } else {
                        // safety fallback if variant not found
                        int current = safeInt(product.getStock());
                        product.setStock(current + qty);
                        productRepo.save(product);
                    }
                }
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepo.save(order);
        return ResponseEntity.ok(toResponse(order));
    }

    // -----------------------------------------------------
    // Other endpoints (unchanged)
    // -----------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return orderRepo.findById(id)
                .map(order -> ResponseEntity.ok(toResponse(order)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<OrderResponse>> getOrdersByUser(@PathVariable Long userId) {
        List<Order> orders = orderRepo.findByUser_Id(userId);
        List<OrderResponse> responses = orders.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // -----------------------------------------------------
    // Mapping
    // -----------------------------------------------------
    private OrderResponse toResponse(Order order) {
        OrderResponse res = new OrderResponse();
        res.setId(order.getId());
        res.setUserName(order.getUser().getName());
        res.setUserPhone(order.getPhoneNumber());
        res.setTotalAmount(order.getTotalAmount());
        res.setShippingAddress(order.getShippingAddress());
        res.setStatus(order.getStatus());
        res.setCreatedAt(order.getCreatedAt());

        if (order.getPayment() != null) {
            res.setPaymentMethod(order.getPayment().getMethod());
            res.setPaymentStatus(order.getPayment().getStatus());
        }

        List<OrderItemResponse> itemResponses = order.getItems().stream().map(i -> {
            OrderItemResponse ir = new OrderItemResponse();
            ir.setProductId(i.getProduct().getId());
            ir.setProductName(i.getProduct().getName());
            ir.setQuantity(i.getQuantity());
            ir.setPrice(i.getPrice());
            ir.setColor(i.getColor());
            ir.setSize(i.getSize());
            return ir;
        }).collect(Collectors.toList());

        res.setItems(itemResponses);
        return res;
    }
}
