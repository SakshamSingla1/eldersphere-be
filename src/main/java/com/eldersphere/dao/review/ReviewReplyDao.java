package com.eldersphere.dao.review;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.ReviewReply;
import com.eldersphere.repositories.ReviewReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewReplyDao implements IDao<ReviewReply, Long> {

    private final ReviewReplyRepository reviewReplyRepository;

    @Override
    public JpaRepository<ReviewReply, Long> getRepository() {
        return reviewReplyRepository;
    }

    public ReviewReply save(ReviewReply reply) {
        return reviewReplyRepository.save(reply);
    }

    public Optional<ReviewReply> findByReviewId(Long reviewId) {
        return reviewReplyRepository.findByReviewId(reviewId);
    }
}
