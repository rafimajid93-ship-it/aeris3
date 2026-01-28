//package com.aeris2.controller;
//
//import com.aeris2.dto.ProductOptionsResponse;
//import com.aeris2.dto.ProductResponse;
//import com.aeris2.dto.ProductVariantResponse;
//import com.aeris2.model.Product;
//import com.aeris2.repository.ProductRepository;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.http.ResponseEntity;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.data.domain.Sort;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.LinkedHashSet;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/products")
//@CrossOrigin
//public class ProductController {
//
//    private final ProductRepository repo;
//
//    public ProductController(ProductRepository repo) {
//        this.repo = repo;
//    }
//
//    @GetMapping
//    @Transactional(readOnly = true)
//    public Page<ProductResponse> list(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "12") int size
//    ) {
//        return repo.findAllWithCategory(
//                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))
//        ).map(this::toResponse);
//    }
//
//    @GetMapping("/{id}")
//    @Transactional(readOnly = true)
//    public ResponseEntity<ProductResponse> detail(@PathVariable Long id) {
//        return repo.findByIdWithCategory(id)
//                .map(p -> ResponseEntity.ok(toResponse(p)))
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    @GetMapping("/{id}/options")
//    @Transactional(readOnly = true)
//    public ResponseEntity<ProductOptionsResponse> options(@PathVariable Long id) {
//        return repo.findByIdWithCategory(id).map(p -> {
//            ProductOptionsResponse r = new ProductOptionsResponse();
//            r.setColors(new ArrayList<>(p.getColors()));
//            r.setSizes(new ArrayList<>(p.getSizes()));
//            return ResponseEntity.ok(r);
//        }).orElse(ResponseEntity.notFound().build());
//    }
//
//    private ProductResponse toResponse(Product p) {
//        ProductResponse r = new ProductResponse();
//
//        r.setId(p.getId());
//        r.setName(p.getName());
//        r.setDescription(p.getDescription());
//        r.setPrice(p.getPrice());
//        r.setStock(p.getStock());
//        r.setImageUrl(p.getImageUrl());
//        r.setPreorder(p.isPreorder());
//
//        r.setReleaseDate(p.getReleaseDate());
//        r.setCreatedAt(p.getCreatedAt());
//        r.setUpdatedAt(p.getUpdatedAt());
//
//        if (p.getCategory() != null) {
//            r.setCategoryId(p.getCategory().getId());
//            r.setCategoryName(p.getCategory().getName());
//        }
//
//        // copy to plain sets (no Hibernate proxies)
//        r.setColors(
//                p.getColors() == null
//                        ? Collections.emptySet()
//                        : new LinkedHashSet<>(p.getColors())
//        );
//        r.setSizes(
//                p.getSizes() == null
//                        ? Collections.emptySet()
//                        : new LinkedHashSet<>(p.getSizes())
//        );
//
//        List<ProductVariantResponse> variants =
//                p.getVariants().stream().map(v -> {
//                    ProductVariantResponse vr = new ProductVariantResponse();
//                    vr.setColor(v.getColor());
//                    vr.setSize(v.getSize());
//                    vr.setStock(v.getStock());
//                    return vr;
//                }).toList();
//
//        r.setVariants(variants);
//        r.setReviewCount(0);
//        return r;
//    }
//}
package com.aeris2.controller;

import com.aeris2.dto.ProductOptionsResponse;
import com.aeris2.dto.ProductResponse;
import com.aeris2.dto.ProductVariantResponse;
import com.aeris2.model.Product;
import com.aeris2.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
//
@RestController
@RequestMapping("/api/products")
@CrossOrigin
public class ProductController {

    private final ProductRepository repo;

    public ProductController(ProductRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public Page<ProductResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) Boolean preorder,
            @RequestParam(required = false, name = "q") String q
    ) {
        // ✅ stable ordering prevents duplicates/missing items between pages
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        return repo.findPaged(preorder, q, pageable)
                .map(this::toResponse);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<ProductResponse> detail(@PathVariable Long id) {
        return repo.findByIdWithCategory(id)
                .map(p -> ResponseEntity.ok(toResponse(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/options")
    @Transactional(readOnly = true)
    public ResponseEntity<ProductOptionsResponse> options(@PathVariable Long id) {
        return repo.findByIdWithCategory(id).map(p -> {
            ProductOptionsResponse r = new ProductOptionsResponse();
            r.setColors(new ArrayList<>(p.getColors()));
            r.setSizes(new ArrayList<>(p.getSizes()));
            return ResponseEntity.ok(r);
        }).orElse(ResponseEntity.notFound().build());
    }

    private ProductResponse toResponse(Product p) {
        ProductResponse r = new ProductResponse();

        r.setId(p.getId());
        r.setName(p.getName());
        r.setDescription(p.getDescription());
        r.setPrice(p.getPrice());
        r.setStock(p.getStock());
        r.setImageUrl(p.getImageUrl());
        r.setPreorder(p.isPreorder());

        r.setReleaseDate(p.getReleaseDate());
        r.setCreatedAt(p.getCreatedAt());
        r.setUpdatedAt(p.getUpdatedAt());

        if (p.getCategory() != null) {
            r.setCategoryId(p.getCategory().getId());
            r.setCategoryName(p.getCategory().getName());
        }

        r.setColors(p.getColors() == null ? Collections.emptySet() : new LinkedHashSet<>(p.getColors()));
        r.setSizes(p.getSizes() == null ? Collections.emptySet() : new LinkedHashSet<>(p.getSizes()));

        List<ProductVariantResponse> variants =
                p.getVariants().stream().map(v -> {
                    ProductVariantResponse vr = new ProductVariantResponse();
                    vr.setColor(v.getColor());
                    vr.setSize(v.getSize());
                    vr.setStock(v.getStock());
                    return vr;
                }).toList();

        r.setVariants(variants);
        r.setReviewCount(0);
        return r;
    }
}
