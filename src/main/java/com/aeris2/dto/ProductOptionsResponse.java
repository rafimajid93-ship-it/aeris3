package com.aeris2.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductOptionsResponse {
    private List<String> colors;
    private List<String> sizes;
}
