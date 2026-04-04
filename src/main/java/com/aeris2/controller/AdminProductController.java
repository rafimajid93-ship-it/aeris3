//package com.aeris2.controller;
//
//import com.aeris2.dto.ProductRequest;
//import com.aeris2.dto.ProductResponse;
//import com.aeris2.dto.ProductVariantRequest;
//import com.aeris2.dto.ProductVariantResponse;
//import com.aeris2.model.Product;
//import com.aeris2.model.ProductVariant;
//import com.aeris2.repository.CategoryRepository;
//import com.aeris2.repository.ProductRepository;
//import com.aeris2.repository.ProductVariantRepository;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.http.ResponseEntity;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.domain.PageRequest;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/admin/products")
//@CrossOrigin
//public class AdminProductController {
//
//    private final ProductRepository productRepo;
//    private final CategoryRepository categoryRepo;
//    private final ProductVariantRepository productVariantRepo;
//
//    public AdminProductController(ProductRepository productRepo,
//                                  CategoryRepository categoryRepo,
//                                  ProductVariantRepository productVariantRepo) {
//        this.productRepo = productRepo;
//        this.categoryRepo = categoryRepo;
//        this.productVariantRepo = productVariantRepo;
//    }
//
//    // -----------------------------------------------------
//    // Helpers
//    // -----------------------------------------------------
//    private String normalizeValue(String raw) {
//        if (raw == null) return "Default";
//        String v = raw.trim();
//        if (v.isEmpty()) return "Default";
//        if (v.equalsIgnoreCase("default") || v.equalsIgnoreCase("free")) {
//            return "Default";
//        }
//        return v;
//    }
//
//    private Set<String> normalizeSet(Collection<String> input) {
//        if (input == null) return Set.of("Default");
//        Set<String> out = new LinkedHashSet<>();
//        for (String v : input) {
//            if (v == null) continue;
//            String n = normalizeValue(v);
//            if (!n.isEmpty()) out.add(n);
//        }
//        if (out.isEmpty()) {
//            out.add("Default");
//        }
//        return out;
//    }
//
//    // -----------------------------------------------------
//    // ✅ List all products (paged)
//    // -----------------------------------------------------
////    @GetMapping
////    @Transactional(readOnly = true)
////    public ResponseEntity<Page<ProductResponse>> getAll(
////            @RequestParam(defaultValue = "0") int page,
////            @RequestParam(defaultValue = "50") int size
////    ) {
////        var res = productRepo.findAllWithCategory(PageRequest.of(page, size))
////                .map(this::toListDto);
////        return ResponseEntity.ok(res);
////    }
//
//    @GetMapping
//    @Transactional(readOnly = true)
//    public ResponseEntity<Page<ProductResponse>> getAll(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "50") int size,
//            @RequestParam(required = false) Boolean preorder,
//            @RequestParam(required = false, name = "q") String q
//    ) {
//        page = Math.max(page, 0);
//
//        // ✅ optional safety cap (adjust if you want)
//        int MAX_SIZE = 200;
//        size = Math.max(1, Math.min(size, MAX_SIZE));
//
//        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
//
//        var res = productRepo.findPaged(preorder, q, pageable)
//                .map(this::toListDto);
//
//        return ResponseEntity.ok(res);
//    }
//
//    // -----------------------------------------------------
//    // ✅ List normal (non-preorder) products
//    // -----------------------------------------------------
//    @GetMapping("/normal")
//    @Transactional(readOnly = true)
//    public ResponseEntity<List<ProductResponse>> getNormalProducts() {
//        var list = productRepo.findAllWithCategory(PageRequest.of(0, 1000))
//                .stream()
//                .filter(p -> !p.isPreorder())
//                .map(this::toListDto)
//                .toList();
//        return ResponseEntity.ok(list);
//    }
//
//    // -----------------------------------------------------
//    // ✅ List preorder products
//    // -----------------------------------------------------
//    @GetMapping("/preorders")
//    @Transactional(readOnly = true)
//    public ResponseEntity<List<ProductResponse>> getPreorderProducts() {
//        var list = productRepo.findAllWithCategory(PageRequest.of(0, 1000))
//                .stream()
//                .filter(Product::isPreorder)
//                .map(this::toListDto)
//                .toList();
//        return ResponseEntity.ok(list);
//    }
//
//    // -----------------------------------------------------
//    // ✅ Create product (normal + preorder)
//    // -----------------------------------------------------
//    @PostMapping
//    @Transactional
//    public ResponseEntity<Product> create(@RequestBody Product p) {
//
//        // Category
//        if (p.getCategory() != null && p.getCategory().getId() != null) {
//            categoryRepo.findById(p.getCategory().getId()).ifPresent(p::setCategory);
//        }
//
//        // Normalize colors / sizes (used both for filters and preorder variants)
//        p.setColors(normalizeSet(p.getColors()));
//        p.setSizes(normalizeSet(p.getSizes()));
//
//        boolean isPreorder = p.isPreorder();
//
//        // Base stock:
//        // - PREORDER: stock = total demand, start from 0, orders will increase.
//        // - NORMAL: keep whatever comes from UI; if variants exist we will recalc below.
//        if (isPreorder) {
//            p.setStock(0);
//        }
//
//        // Grab variants from request (used only for NORMAL products)
//        List<ProductVariant> requestVariants =
//                p.getVariants() != null ? new ArrayList<>(p.getVariants()) : new ArrayList<>();
//
//        // Clear variants before saving; we'll attach them after we have product id
//        p.setVariants(new ArrayList<>());
//
//        // Save base product
//        Product saved = productRepo.save(p);
//
//        // ---------- NORMAL PRODUCT: variants from request ----------
//        if (!isPreorder && !requestVariants.isEmpty()) {
//
//            for (ProductVariant v : requestVariants) {
//                String color = normalizeValue(v.getColor());
//                String size = normalizeValue(v.getSize());
//                v.setColor(color);
//                v.setSize(size);
//                v.setProduct(saved);
//                // stock is primitive int; if admin left blank it will be 0.
//            }
//
//            productVariantRepo.saveAll(requestVariants);
//            saved.setVariants(requestVariants);
//
//            int totalStock = requestVariants.stream()
//                    .mapToInt(ProductVariant::getStock)
//                    .sum();
//            saved.setStock(totalStock);           // overwrite with sum of variants
//            productRepo.save(saved);
//        }
//
//        // ---------- PREORDER PRODUCT: generate variants from colors/sizes ----------
//        if (isPreorder) {
//            Set<String> colors = saved.getColors();
//            Set<String> sizes = saved.getSizes();
//
//            boolean hasNonDefault =
//                    colors.stream().anyMatch(c -> !c.equalsIgnoreCase("Default")) ||
//                            sizes.stream().anyMatch(s -> !s.equalsIgnoreCase("Default"));
//
//            if (hasNonDefault) {
//                List<ProductVariant> preorderVariants = new ArrayList<>();
//                for (String c : colors) {
//                    for (String s : sizes) {
//                        ProductVariant v = new ProductVariant();
//                        v.setProduct(saved);
//                        v.setColor(normalizeValue(c));
//                        v.setSize(normalizeValue(s));
//                        v.setStock(0);   // demand starts at zero
//                        preorderVariants.add(v);
//                    }
//                }
//                productVariantRepo.saveAll(preorderVariants);
//                saved.setVariants(preorderVariants);
//            }
//
//            // saved.stock stays 0; OrderController will increase it on each preorder.
//        }
//
//        return ResponseEntity.ok(saved);
//    }
//
//    // -----------------------------------------------------
//// ✅ Update product (no unique-key errors, no orphan bug)
//// -----------------------------------------------------
//    @PutMapping("/{id}")
//    @Transactional
//    public ResponseEntity<Product> update(@PathVariable Long id, @RequestBody ProductRequest p) {
//        return productRepo.findById(id).map(existing -> {
//
//            // ---------- Basic fields ----------
//            if (p.getName() != null) existing.setName(p.getName());
//            if (p.getDescription() != null) existing.setDescription(p.getDescription());
//            if (p.getPrice() != null) existing.setPrice(p.getPrice());
//            if (p.getImageUrl() != null) existing.setImageUrl(p.getImageUrl());
//
//            // ---------- Colors & Sizes ----------
//            if (p.getColors() != null) {
//                existing.setColors(normalizeSet(p.getColors()));
//            } else if (existing.getColors() == null || existing.getColors().isEmpty()) {
//                existing.setColors(Set.of("Default"));
//            }
//
//            if (p.getSizes() != null) {
//                existing.setSizes(normalizeSet(p.getSizes()));
//            } else if (existing.getSizes() == null || existing.getSizes().isEmpty()) {
//                existing.setSizes(Set.of("Default"));
//            }
//
//            // ---------- Category ----------
//            if (p.getCategoryId() != null) {
//                categoryRepo.findById(p.getCategoryId()).ifPresent(existing::setCategory);
//            }
//
//            // ---------- PREORDER / NORMAL SWITCH ----------
//            if (p.getPreorder() != null) {
//
//                if (Boolean.TRUE.equals(p.getPreorder())) {
//                    // ===== PREORDER PRODUCT =====
//                    existing.setPreorder(true);
//                    existing.setReleaseDate(p.getReleaseDate());
//
//                    // Keep existing demand; only ensure variant matrix exists if real options
//                    Set<String> colors = existing.getColors();
//                    Set<String> sizes  = existing.getSizes();
//                    boolean hasNonDefault =
//                            colors.stream().anyMatch(c -> !c.equalsIgnoreCase("Default")) ||
//                                    sizes.stream().anyMatch(s -> !s.equalsIgnoreCase("Default"));
//
//                    if (hasNonDefault) {
//                        List<ProductVariant> current = existing.getVariants(); // managed list
//                        if (current.isEmpty()) {
//                            for (String c : colors) {
//                                for (String s : sizes) {
//                                    ProductVariant v = new ProductVariant();
//                                    v.setProduct(existing);
//                                    v.setColor(normalizeValue(c));
//                                    v.setSize(normalizeValue(s));
//                                    v.setStock(0); // demand starts ⟂
//                                    current.add(v);
//                                }
//                            }
//                        }
//                    }
//                    // existing.stock already equals total demand → do not touch.
//
//                } else {
//                    // ===== NORMAL PRODUCT =====
//                    existing.setPreorder(false);
//                    existing.setReleaseDate(null);
//
//                    // Work only with the managed collection (orphanRemoval=true)
//                    List<ProductVariant> current = existing.getVariants(); // same instance Hibernate tracks
//
//                    // Index existing variants by normalized (color|size)
//                    Map<String, ProductVariant> existingByKey = new LinkedHashMap<>();
//                    for (ProductVariant v : current) {
//                        String key = normalizeValue(v.getColor()) + "|" + normalizeValue(v.getSize());
//                        existingByKey.put(key, v);
//                    }
//
//                    List<ProductVariant> newList = new ArrayList<>();
//                    int totalStock = 0;
//
//                    if (p.getVariants() != null && !p.getVariants().isEmpty()) {
//
//                        // Aggregate payload by (color,size) so we never insert duplicates
//                        Map<String, Integer> aggregated = new LinkedHashMap<>();
//
//                        for (ProductVariantRequest vReq : p.getVariants()) {
//                            String color = normalizeValue(vReq.getColor());
//                            String size  = normalizeValue(vReq.getSize());
//                            int stock    = Math.max(0, vReq.getStock());
//
//                            String key = color + "|" + size;
//                            aggregated.merge(key, stock, Integer::sum);
//                        }
//
//                        // Update or create variants from aggregated input
//                        for (Map.Entry<String, Integer> entry : aggregated.entrySet()) {
//                            String key = entry.getKey();
//                            String[] parts = key.split("\\|", 2);
//                            String color = parts[0];
//                            String size  = parts[1];
//                            int stock    = entry.getValue();
//
//                            ProductVariant v = existingByKey.get(key); // null = new
//                            if (v == null) {
//                                v = new ProductVariant();
//                                v.setProduct(existing);
//                                v.setColor(color);
//                                v.setSize(size);
//                            }
//                            v.setStock(stock);
//                            newList.add(v);
//                            totalStock += stock;
//                        }
//                    }
//
//                    // Mutate the managed collection in place (orphanRemoval will delete old ones)
//                    current.clear();
//                    current.addAll(newList);
//                    existing.setStock(totalStock);
//                }
//            }
//
//            Product saved = productRepo.save(existing); // cascades variants
//            return ResponseEntity.ok(saved);
//
//        }).orElse(ResponseEntity.notFound().build());
//    }
//
//
//    // -----------------------------------------------------
//    // ✅ Delete product
//    // -----------------------------------------------------
//    @DeleteMapping("/{id}")
//    @Transactional
//    public ResponseEntity<Void> delete(@PathVariable Long id) {
//        if (!productRepo.existsById(id)) return ResponseEntity.notFound().build();
//        productVariantRepo.deleteByProductId(id);
//        productRepo.deleteById(id);
//        return ResponseEntity.noContent().build();
//    }
//
//    // -----------------------------------------------------
//    // ✅ Get single product (with category, variants, colors, sizes)
//    // -----------------------------------------------------
//    @GetMapping("/{id}")
//    @Transactional(readOnly = true)
//    public ResponseEntity<ProductResponse> getByIdAdmin(@PathVariable Long id) {
//        return productRepo.findByIdWithCategory(id)
//                .map(p -> ResponseEntity.ok(toListDto(p)))
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    // -----------------------------------------------------
//    // 🧩 DTO mapper
//    // -----------------------------------------------------
//    private ProductResponse toListDto(Product p) {
//        ProductResponse dto = new ProductResponse();
//        dto.setId(p.getId());
//        dto.setName(p.getName());
//        dto.setDescription(p.getDescription());
//        dto.setPrice(p.getPrice());
//        dto.setStock(p.getStock());
//        dto.setImageUrl(p.getImageUrl());
//        dto.setPreorder(p.isPreorder());
//        dto.setReleaseDate(p.getReleaseDate());
//        dto.setCreatedAt(p.getCreatedAt());
//        dto.setUpdatedAt(p.getUpdatedAt());
//
//        // Colors / Sizes for frontend
//        dto.setColors(
//                p.getColors() != null && !p.getColors().isEmpty()
//                        ? p.getColors()
//                        : Set.of("Default")
//        );
//        dto.setSizes(
//                p.getSizes() != null && !p.getSizes().isEmpty()
//                        ? p.getSizes()
//                        : Set.of("Default")
//        );
//
//        // Variants (normalize values on the way out)
//        if (p.getVariants() != null && !p.getVariants().isEmpty()) {
//            List<ProductVariantResponse> varRes = p.getVariants().stream().map(v -> {
//                ProductVariantResponse vr = new ProductVariantResponse();
//                vr.setColor(v.getColor() != null ? normalizeValue(v.getColor()) : "Default");
//                vr.setSize(v.getSize() != null ? normalizeValue(v.getSize()) : "Default");
//                vr.setStock(v.getStock());
//                return vr;
//            }).collect(Collectors.toList());
//            dto.setVariants(varRes);
//        } else {
//            dto.setVariants(List.of());
//        }
//
//        if (p.getCategory() != null) {
//            dto.setCategoryId(p.getCategory().getId());
//            dto.setCategoryName(p.getCategory().getName());
//        } else {
//            dto.setCategoryName("-");
//        }
//
//        return dto;
//    }
//}
package com.aeris2.controller;

import com.aeris2.dto.ProductImageResponse;
import com.aeris2.dto.ProductRequest;
import com.aeris2.dto.ProductResponse;
import com.aeris2.dto.ProductVariantRequest;
import com.aeris2.dto.ProductVariantResponse;
import com.aeris2.model.Product;
import com.aeris2.model.ProductImage;
import com.aeris2.model.ProductVariant;
import com.aeris2.repository.CategoryRepository;
import com.aeris2.repository.ProductRepository;
import com.aeris2.repository.ProductVariantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/products")
@CrossOrigin
public class AdminProductController {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final ProductVariantRepository productVariantRepo;

    public AdminProductController(ProductRepository productRepo,
                                  CategoryRepository categoryRepo,
                                  ProductVariantRepository productVariantRepo) {
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
        this.productVariantRepo = productVariantRepo;
    }

    private String normalizeValue(String raw) {
        if (raw == null) return "Default";
        String v = raw.trim();
        if (v.isEmpty()) return "Default";
        if (v.equalsIgnoreCase("default") || v.equalsIgnoreCase("free")) {
            return "Default";
        }
        return v;
    }

    private Set<String> normalizeSet(Collection<String> input) {
        if (input == null) return Set.of("Default");
        Set<String> out = new LinkedHashSet<>();
        for (String v : input) {
            if (v == null) continue;
            String n = normalizeValue(v);
            if (!n.isEmpty()) out.add(n);
        }
        if (out.isEmpty()) {
            out.add("Default");
        }
        return out;
    }

    private List<String> normalizeImageUrls(List<String> input, String fallbackSingleUrl) {
        List<String> out = new ArrayList<>();

        if (input != null) {
            for (String url : input) {
                if (url == null) continue;
                String trimmed = url.trim();
                if (!trimmed.isEmpty()) out.add(trimmed);
            }
        }

        if (out.isEmpty() && fallbackSingleUrl != null && !fallbackSingleUrl.trim().isEmpty()) {
            out.add(fallbackSingleUrl.trim());
        }

        return out;
    }

    private void rebuildImages(Product product, List<String> urls) {
        product.getImages().clear();

        int order = 0;
        for (String url : urls) {
            ProductImage image = new ProductImage();
            image.setProduct(product);
            image.setImageUrl(url);
            image.setSortOrder(order++);
            product.getImages().add(image);
        }

        product.setImageUrl(urls.isEmpty() ? null : urls.get(0));
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Page<ProductResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) Boolean preorder,
            @RequestParam(required = false, name = "q") String q
    ) {
        page = Math.max(page, 0);
        int MAX_SIZE = 200;
        size = Math.max(1, Math.min(size, MAX_SIZE));

        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        var res = productRepo.findPaged(preorder, q, pageable)
                .map(this::toListDto);

        return ResponseEntity.ok(res);
    }

    @GetMapping("/normal")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ProductResponse>> getNormalProducts() {
        var list = productRepo.findAllWithCategory(PageRequest.of(0, 1000))
                .stream()
                .filter(p -> !p.isPreorder())
                .map(this::toListDto)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/preorders")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ProductResponse>> getPreorderProducts() {
        var list = productRepo.findAllWithCategory(PageRequest.of(0, 1000))
                .stream()
                .filter(Product::isPreorder)
                .map(this::toListDto)
                .toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ProductResponse> create(@RequestBody Product p) {

        if (p.getCategory() != null && p.getCategory().getId() != null) {
            categoryRepo.findById(p.getCategory().getId()).ifPresent(p::setCategory);
        }

        p.setColors(normalizeSet(p.getColors()));
        p.setSizes(normalizeSet(p.getSizes()));

        boolean isPreorder = p.isPreorder();

        if (isPreorder) {
            p.setStock(0);
        }

        List<ProductVariant> requestVariants =
                p.getVariants() != null ? new ArrayList<>(p.getVariants()) : new ArrayList<>();

        p.setVariants(new ArrayList<>());

        List<ProductImage> requestImages =
                p.getImages() != null ? new ArrayList<>(p.getImages()) : new ArrayList<>();
        p.setImages(new ArrayList<>());

        Product saved = productRepo.save(p);

        if (!requestImages.isEmpty()) {
            int order = 0;
            for (ProductImage image : requestImages) {
                if (image.getImageUrl() == null || image.getImageUrl().trim().isEmpty()) continue;
                image.setId(null);
                image.setProduct(saved);
                image.setImageUrl(image.getImageUrl().trim());
                image.setSortOrder(order++);
                saved.getImages().add(image);
            }
            if (!saved.getImages().isEmpty()) {
                saved.setImageUrl(saved.getImages().get(0).getImageUrl());
            }
        } else if (saved.getImageUrl() != null && !saved.getImageUrl().trim().isEmpty()) {
            ProductImage cover = new ProductImage();
            cover.setProduct(saved);
            cover.setImageUrl(saved.getImageUrl().trim());
            cover.setSortOrder(0);
            saved.getImages().add(cover);
        }

        if (!isPreorder && !requestVariants.isEmpty()) {
            for (ProductVariant v : requestVariants) {
                String color = normalizeValue(v.getColor());
                String size = normalizeValue(v.getSize());
                v.setColor(color);
                v.setSize(size);
                v.setProduct(saved);
            }

            productVariantRepo.saveAll(requestVariants);
            saved.setVariants(requestVariants);

            int totalStock = requestVariants.stream()
                    .mapToInt(ProductVariant::getStock)
                    .sum();
            saved.setStock(totalStock);
        }

        if (isPreorder) {
            Set<String> colors = saved.getColors();
            Set<String> sizes = saved.getSizes();

            boolean hasNonDefault =
                    colors.stream().anyMatch(c -> !c.equalsIgnoreCase("Default")) ||
                            sizes.stream().anyMatch(s -> !s.equalsIgnoreCase("Default"));

            if (hasNonDefault) {
                List<ProductVariant> preorderVariants = new ArrayList<>();
                for (String c : colors) {
                    for (String s : sizes) {
                        ProductVariant v = new ProductVariant();
                        v.setProduct(saved);
                        v.setColor(normalizeValue(c));
                        v.setSize(normalizeValue(s));
                        v.setStock(0);
                        preorderVariants.add(v);
                    }
                }
                productVariantRepo.saveAll(preorderVariants);
                saved.setVariants(preorderVariants);
            }
        }

        Product finalSaved = productRepo.save(saved);
        return ResponseEntity.ok(toListDto(finalSaved));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ProductResponse> update(@PathVariable Long id, @RequestBody ProductRequest p) {
        return productRepo.findByIdWithCategory(id).map(existing -> {

            if (p.getName() != null) existing.setName(p.getName());
            if (p.getDescription() != null) existing.setDescription(p.getDescription());
            if (p.getPrice() != null) existing.setPrice(p.getPrice());

            List<String> normalizedImageUrls = normalizeImageUrls(p.getImageUrls(), p.getImageUrl());
            if (!normalizedImageUrls.isEmpty()) {
                rebuildImages(existing, normalizedImageUrls);
            } else if (p.getImageUrl() != null && p.getImageUrl().trim().isEmpty()) {
                existing.getImages().clear();
                existing.setImageUrl(null);
            }

            if (p.getColors() != null) {
                existing.setColors(normalizeSet(p.getColors()));
            } else if (existing.getColors() == null || existing.getColors().isEmpty()) {
                existing.setColors(Set.of("Default"));
            }

            if (p.getSizes() != null) {
                existing.setSizes(normalizeSet(p.getSizes()));
            } else if (existing.getSizes() == null || existing.getSizes().isEmpty()) {
                existing.setSizes(Set.of("Default"));
            }

            if (p.getCategoryId() != null) {
                categoryRepo.findById(p.getCategoryId()).ifPresent(existing::setCategory);
            }

            if (p.getPreorder() != null) {
                if (Boolean.TRUE.equals(p.getPreorder())) {
                    existing.setPreorder(true);
                    existing.setReleaseDate(p.getReleaseDate());

                    Set<String> colors = existing.getColors();
                    Set<String> sizes = existing.getSizes();

                    boolean hasNonDefault =
                            colors.stream().anyMatch(c -> !c.equalsIgnoreCase("Default")) ||
                                    sizes.stream().anyMatch(s -> !s.equalsIgnoreCase("Default"));

                    if (hasNonDefault) {
                        List<ProductVariant> current = existing.getVariants();
                        if (current.isEmpty()) {
                            for (String c : colors) {
                                for (String s : sizes) {
                                    ProductVariant v = new ProductVariant();
                                    v.setProduct(existing);
                                    v.setColor(normalizeValue(c));
                                    v.setSize(normalizeValue(s));
                                    v.setStock(0);
                                    current.add(v);
                                }
                            }
                        }
                    }

                } else {
                    existing.setPreorder(false);
                    existing.setReleaseDate(null);

                    List<ProductVariant> current = existing.getVariants();

                    Map<String, ProductVariant> existingByKey = new LinkedHashMap<>();
                    for (ProductVariant v : current) {
                        String key = normalizeValue(v.getColor()) + "|" + normalizeValue(v.getSize());
                        existingByKey.put(key, v);
                    }

                    List<ProductVariant> newList = new ArrayList<>();
                    int totalStock = 0;

                    if (p.getVariants() != null && !p.getVariants().isEmpty()) {
                        Map<String, Integer> aggregated = new LinkedHashMap<>();

                        for (ProductVariantRequest vReq : p.getVariants()) {
                            String color = normalizeValue(vReq.getColor());
                            String size = normalizeValue(vReq.getSize());
                            int stock = Math.max(0, vReq.getStock());

                            String key = color + "|" + size;
                            aggregated.merge(key, stock, Integer::sum);
                        }

                        for (Map.Entry<String, Integer> entry : aggregated.entrySet()) {
                            String key = entry.getKey();
                            String[] parts = key.split("\\|", 2);
                            String color = parts[0];
                            String size = parts[1];
                            int stock = entry.getValue();

                            ProductVariant v = existingByKey.get(key);
                            if (v == null) {
                                v = new ProductVariant();
                                v.setProduct(existing);
                                v.setColor(color);
                                v.setSize(size);
                            }
                            v.setStock(stock);
                            newList.add(v);
                            totalStock += stock;
                        }
                    }

                    current.clear();
                    current.addAll(newList);
                    existing.setStock(totalStock);
                }
            }

            Product saved = productRepo.save(existing);
            return ResponseEntity.ok(toListDto(saved));

        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!productRepo.existsById(id)) return ResponseEntity.notFound().build();
        productVariantRepo.deleteByProductId(id);
        productRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<ProductResponse> getByIdAdmin(@PathVariable Long id) {
        return productRepo.findByIdWithCategory(id)
                .map(p -> ResponseEntity.ok(toListDto(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    private ProductResponse toListDto(Product p) {
        ProductResponse dto = new ProductResponse();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setPrice(p.getPrice());
        dto.setStock(p.getStock());
        dto.setImageUrl(resolveCoverImage(p));
        dto.setPreorder(p.isPreorder());
        dto.setReleaseDate(p.getReleaseDate());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());

        dto.setColors(
                p.getColors() != null && !p.getColors().isEmpty()
                        ? p.getColors()
                        : Set.of("Default")
        );
        dto.setSizes(
                p.getSizes() != null && !p.getSizes().isEmpty()
                        ? p.getSizes()
                        : Set.of("Default")
        );

        if (p.getVariants() != null && !p.getVariants().isEmpty()) {
            List<ProductVariantResponse> varRes = p.getVariants().stream().map(v -> {
                ProductVariantResponse vr = new ProductVariantResponse();
                vr.setColor(v.getColor() != null ? normalizeValue(v.getColor()) : "Default");
                vr.setSize(v.getSize() != null ? normalizeValue(v.getSize()) : "Default");
                vr.setStock(v.getStock());
                return vr;
            }).collect(Collectors.toList());
            dto.setVariants(varRes);
        } else {
            dto.setVariants(List.of());
        }

        List<ProductImageResponse> images = p.getImages() == null
                ? List.of()
                : p.getImages().stream().map(img -> {
            ProductImageResponse ir = new ProductImageResponse();
            ir.setId(img.getId());
            ir.setImageUrl(img.getImageUrl());
            ir.setSortOrder(img.getSortOrder());
            return ir;
        }).toList();

        dto.setImages(images);
        dto.setImageUrls(images.stream().map(ProductImageResponse::getImageUrl).toList());

        if (p.getCategory() != null) {
            dto.setCategoryId(p.getCategory().getId());
            dto.setCategoryName(p.getCategory().getName());
        } else {
            dto.setCategoryName("-");
        }

        return dto;
    }

    private String resolveCoverImage(Product p) {
        if (p.getImages() != null && !p.getImages().isEmpty()) {
            return p.getImages().get(0).getImageUrl();
        }
        return p.getImageUrl();
    }
}