package com.eldersphere.dao.review;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.Review;
import com.eldersphere.repositories.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewDao implements IDao<Review, Long> {

    private final ReviewRepository reviewRepository;

    @Override
    public JpaRepository<Review, Long> getRepository() {
        return reviewRepository;
    }

    public Review save(Review review) {
        return reviewRepository.save(review);
    }

    public Page<Review> findByCaretakerId(Long caretakerId, Pageable pageable) {
        return reviewRepository.findByCaretakerId(caretakerId, pageable);
    }

    public Page<Review> findByReviewerId(Long reviewerId, Pageable pageable) {
        return reviewRepository.findByReviewerId(reviewerId, pageable);
    }

    public Optional<Review> findByBookingId(Long bookingId) {
        return reviewRepository.findByBookingId(bookingId);
    }

    public boolean existsByBookingId(Long bookingId) {
        return reviewRepository.existsByBookingId(bookingId);
    }

    public Double findAverageRatingByCaretakerId(Long caretakerId) {
        return reviewRepository.findAverageRatingByCaretakerId(caretakerId);
    }
}
