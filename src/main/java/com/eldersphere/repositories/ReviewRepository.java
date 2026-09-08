package com.eldersphere.repositories;

import com.eldersphere.entities.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByCaretakerId(Long caretakerId, Pageable pageable);
    Page<Review> findByReviewerId(Long reviewerId, Pageable pageable);
    Optional<Review> findByBookingId(Long bookingId);
    boolean existsByBookingId(Long bookingId);

    @org.springframework.data.jpa.repository.Query("SELECT AVG(r.rating) FROM Review r WHERE r.caretakerId = :caretakerId")
    Double findAverageRatingByCaretakerId(@org.springframework.data.repository.query.Param("caretakerId") Long caretakerId);
}
