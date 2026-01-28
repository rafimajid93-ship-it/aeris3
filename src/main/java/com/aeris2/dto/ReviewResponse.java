package com.aeris2.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class ReviewResponse {
    private Long id;
    private String userName;
    private int rating;
    private String comment;
    private Instant createdAt;
}
