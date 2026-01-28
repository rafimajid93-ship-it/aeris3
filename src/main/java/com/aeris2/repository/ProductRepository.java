//package com.aeris2.repository;
//
//import com.aeris2.model.Product;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.EntityGraph;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import java.util.Optional;
//
//public interface ProductRepository extends JpaRepository<Product, Long> {
//
//    // LIST: only fetch single-valued association (category) – safe with pagination
//    @EntityGraph(attributePaths = {"category"})
//    @Query("SELECT p FROM Product p")
//    Page<Product> findAllWithCategory(Pageable pageable);
//
//    // DETAIL: fetch everything we need for a single product
//    @EntityGraph(attributePaths = {"category", "variants", "colors", "sizes"})
//    @Query("SELECT p FROM Product p WHERE p.id = :id")
//    Optional<Product> findByIdWithCategory(@Param("id") Long id);
//}

package com.aeris2.repository;

import com.aeris2.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"category"})
    @Query("""
        SELECT p
        FROM Product p
        WHERE (:preorder IS NULL OR p.preorder = :preorder)
          AND (
            :q IS NULL OR :q = '' OR
            LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(p.description) LIKE LOWER(CONCAT('%', :q, '%')) OR
            CAST(p.id AS string) LIKE CONCAT('%', :q, '%')
          )
    """)
    Page<Product> findPaged(
            @Param("preorder") Boolean preorder,
            @Param("q") String q,
            Pageable pageable
    );
    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT p FROM Product p")
    Page<Product> findAllWithCategory(Pageable pageable);

    @EntityGraph(attributePaths = {"category", "variants", "colors", "sizes"})
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithCategory(@Param("id") Long id);
}
