package com.aeris2.repository;

import com.aeris2.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    /**
     * Find a single variant for a product by color/size (case-insensitive).
     * Used by OrderController for both normal and preorder logic.
     */
    Optional<ProductVariant> findByProductIdAndColorIgnoreCaseAndSizeIgnoreCase(
            Long productId,
            String color,
            String size
    );

    /**
     * All variants of a product – used for stock / demand aggregation.
     */
    List<ProductVariant> findByProductId(Long productId);

    /**
     * Delete all variants of a product – used in AdminProductController
     * when switching / rebuilding normal product variants.
     */
    void deleteByProductId(Long productId);
}
