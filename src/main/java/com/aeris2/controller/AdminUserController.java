package com.aeris2.controller;
//
import com.aeris2.dto.UserResponse;
import com.aeris2.model.User;
import com.aeris2.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin
public class AdminUserController {

    private final UserRepository userRepo;

    public AdminUserController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        var users = userRepo.findAll().stream().map(u -> {
            UserResponse dto = new UserResponse();
            dto.setId(u.getId());
            dto.setName(u.getName());
            dto.setEmail(u.getEmail());
            dto.setPhone(u.getPhone());
            dto.setAddress(u.getAddress());
            dto.setRole(u.getRole());
            return dto;
        }).toList();

        return ResponseEntity.ok(users);
    }
}
