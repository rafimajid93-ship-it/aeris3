package com.aeris2.controller;
//
import com.aeris2.repository.OrderRepository;
import com.aeris2.repository.ProductRepository;
import com.aeris2.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/analytics")
@CrossOrigin
public class AdminAnalyticsController {

    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;

    public AdminAnalyticsController(OrderRepository orderRepo, ProductRepository productRepo, UserRepository userRepo) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
        this.userRepo = userRepo;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        // ✅ Total orders
        long totalOrders = orderRepo.count();

        // ✅ Total customers (only users with role USER)
        long totalCustomers = userRepo.countByRole("USER");

        // ✅ Total products in stock
        long totalProducts = productRepo.count();

        // ✅ Total revenue (sum of order total amounts)
        BigDecimal totalRevenue = orderRepo.sumAllOrderTotals().orElse(BigDecimal.ZERO);

        return ResponseEntity.ok(Map.of(
                "totalOrders", totalOrders,
                "totalCustomers", totalCustomers,
                "totalProducts", totalProducts,
                "totalRevenue", totalRevenue
        ));
    }
}
