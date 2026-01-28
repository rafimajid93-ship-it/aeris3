package com.aeris2.repository;


import com.aeris2.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findAllByProduct_Id(Long productId, Pageable pageable);
    Page<Review> findAllByUser_Id(Long userId, Pageable pageable);
}
