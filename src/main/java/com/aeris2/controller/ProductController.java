//package com.aeris2.controller;
//
//import com.aeris2.dto.ProductOptionsResponse;
//import com.aeris2.dto.ProductResponse;
//import com.aeris2.dto.ProductVariantResponse;
//import com.aeris2.model.Product;
//import com.aeris2.repository.ProductRepository;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Sort;
//import org.springframework.http.ResponseEntity;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.*;
////
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
//            @RequestParam(defaultValue = "12") int size,
//            @RequestParam(required = false) Boolean preorder,
//            @RequestParam(required = false, name = "q") String q
//    ) {
//        // ✅ stable ordering prevents duplicates/missing items between pages
//        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
//
//        return repo.findPaged(preorder, q, pageable)
//                .map(this::toResponse);
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
//        r.setColors(p.getColors() == null ? Collections.emptySet() : new LinkedHashSet<>(p.getColors()));
//        r.setSizes(p.getSizes() == null ? Collections.emptySet() : new LinkedHashSet<>(p.getSizes()));
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
//package com.aeris2.controller;
//
//import com.aeris2.dto.ProductOptionsResponse;
//import com.aeris2.dto.ProductResponse;
//import com.aeris2.dto.ProductVariantResponse;
//import com.aeris2.model.Product;
//import com.aeris2.repository.ProductRepository;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Sort;
//import org.springframework.http.ResponseEntity;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.*;
//import java.util.stream.Collectors;
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
//    private String normalizeValue(String raw) {
//        if (raw == null) return "Default";
//        String v = raw.trim();
//        if (v.isEmpty()) return "Default";
//        if (v.equalsIgnoreCase("default")) return "Default";
//        return v;
//    }
//
//    private Set<String> normalizeSet(Set<String> in) {
//        if (in == null || in.isEmpty()) return Set.of("Default");
//        LinkedHashSet<String> out = new LinkedHashSet<>();
//        for (String s : in) out.add(normalizeValue(s));
//        if (out.isEmpty()) out.add("Default");
//        return out;
//    }
//
//    @GetMapping
//    @Transactional(readOnly = true)
//    public Page<ProductResponse> list(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "12") int size,
//            @RequestParam(required = false) Boolean preorder,
//            @RequestParam(required = false, name = "q") String q
//    ) {
//        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
//
//        return repo.findPaged(preorder, q, pageable)
//                .map(this::toResponse);
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
//            r.setColors(new ArrayList<>(normalizeSet(p.getColors())));
//            r.setSizes(new ArrayList<>(normalizeSet(p.getSizes())));
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
//        r.setColors(normalizeSet(p.getColors()));
//        r.setSizes(normalizeSet(p.getSizes()));
//
//        List<ProductVariantResponse> variants =
//                Optional.ofNullable(p.getVariants()).orElse(List.of())
//                        .stream()
//                        .map(v -> {
//                            ProductVariantResponse vr = new ProductVariantResponse();
//                            vr.setColor(normalizeValue(v.getColor()));
//                            vr.setSize(normalizeValue(v.getSize()));
//                            vr.setStock(v.getStock());
//                            return vr;
//                        })
//                        .toList();
//
//        r.setVariants(variants);
//        r.setReviewCount(0);
//        return r;
//    }
//}

//

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

////package com.aeris2.repository;
////
////import com.aeris2.model.Product;
////import org.springframework.data.domain.Page;
////import org.springframework.data.domain.Pageable;
////import org.springframework.data.jpa.repository.EntityGraph;
////import org.springframework.data.jpa.repository.JpaRepository;
////import org.springframework.data.jpa.repository.Query;
////import org.springframework.data.repository.query.Param;
////
////import java.util.Optional;
////
////public interface ProductRepository extends JpaRepository<Product, Long> {
////
////    // LIST: only fetch single-valued association (category) – safe with pagination
////    @EntityGraph(attributePaths = {"category"})
////    @Query("SELECT p FROM Product p")
////    Page<Product> findAllWithCategory(Pageable pageable);
////
////    // DETAIL: fetch everything we need for a single product
////    @EntityGraph(attributePaths = {"category", "variants", "colors", "sizes"})
////    @Query("SELECT p FROM Product p WHERE p.id = :id")
////    Optional<Product> findByIdWithCategory(@Param("id") Long id);
////}
//
//package com.aeris2.repository;
//
//        import com.aeris2.model.Product;
//        import org.springframework.data.domain.Page;
//        import org.springframework.data.domain.Pageable;
//        import org.springframework.data.jpa.repository.EntityGraph;
//        import org.springframework.data.jpa.repository.JpaRepository;
//        import org.springframework.data.jpa.repository.Query;
//        import org.springframework.data.repository.query.Param;
//
//        import java.util.Optional;
//
//public interface ProductRepository extends JpaRepository<Product, Long> {
//
//    @EntityGraph(attributePaths = {"category"})
//    @Query("""
//        SELECT p
//        FROM Product p
//        WHERE (:preorder IS NULL OR p.preorder = :preorder)
//          AND (
//            :q IS NULL OR :q = '' OR
//            LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')) OR
//            LOWER(p.description) LIKE LOWER(CONCAT('%', :q, '%')) OR
//            CAST(p.id AS string) LIKE CONCAT('%', :q, '%')
//          )
//    """)
//    Page<Product> findPaged(
//            @Param("preorder") Boolean preorder,
//            @Param("q") String q,
//            Pageable pageable
//    );
//    @EntityGraph(attributePaths = {"category"})
//    @Query("SELECT p FROM Product p")
//    Page<Product> findAllWithCategory(Pageable pageable);
//
//    @EntityGraph(attributePaths = {"category", "variants", "colors", "sizes"})
//    @Query("SELECT p FROM Product p WHERE p.id = :id")
//    Optional<Product> findByIdWithCategory(@Param("id") Long id);
//}
