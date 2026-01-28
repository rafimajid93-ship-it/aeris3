package com.aeris2.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Consider "fullName" if you prefer
    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false, length = 190)
    private String email;

    @Column(nullable = false)
    private String password;

    private String phone;

    @Column(length = 1000)
    private String address;

    /** USER / ADMIN */
    @Column(nullable = false, length = 20)
    private String role;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Order> orders = new ArrayList<>();
}
