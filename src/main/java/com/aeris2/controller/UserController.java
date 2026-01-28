package com.aeris2.controller;
//
import com.aeris2.dto.OrderResponse;
import com.aeris2.dto.UserRequest;
import com.aeris2.dto.UserResponse;
import com.aeris2.model.User;
import com.aeris2.repository.OrderRepository;
import com.aeris2.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {

    private final UserRepository userRepo;
    private final OrderRepository orderRepo;
    private final PasswordEncoder encoder;

    public UserController(UserRepository userRepo, OrderRepository orderRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.orderRepo = orderRepo;
        this.encoder = encoder;
    }

    // -------------------- Profile --------------------

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getProfile(Authentication auth) {
        String email = auth.getName();
        User user = userRepo.findByEmail(email).orElseThrow();
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(Authentication auth,
                                                      @RequestBody UserRequest req) {
        String email = auth.getName();
        User user = userRepo.findByEmail(email).orElseThrow();

        if (req.getName() != null) user.setName(req.getName());
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getAddress() != null) user.setAddress(req.getAddress());
        if (req.getPassword() != null && !req.getPassword().isEmpty()) {
            user.setPassword(encoder.encode(req.getPassword()));
        }

        userRepo.save(user);
        return ResponseEntity.ok(toResponse(user));
    }

    // -------------------- My Orders --------------------

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> myOrders(Authentication auth) {
        String email = auth.getName();
        User user = userRepo.findByEmail(email).orElseThrow();

        var orders = orderRepo.findAllByUser_Id(user.getId());
        List<OrderResponse> responses = orders.stream()
                .map(o -> {
                    OrderResponse r = new OrderResponse();
                    r.setId(o.getId());
                    r.setTotalAmount(o.getTotalAmount());
                    r.setStatus(o.getStatus());
                    r.setCreatedAt(o.getCreatedAt());
                    r.setShippingAddress(o.getShippingAddress());
                    return r;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    // -------------------- Helper --------------------
    private UserResponse toResponse(User u) {
        UserResponse res = new UserResponse();
        res.setId(u.getId());
        res.setName(u.getName());
        res.setEmail(u.getEmail());
        res.setPhone(u.getPhone());
        res.setAddress(u.getAddress());
        res.setRole(u.getRole());
        return res;
    }
}
