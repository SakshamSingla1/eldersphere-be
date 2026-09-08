package com.eldersphere.services.impl;

import com.eldersphere.dao.booking.BookingDao;
import com.eldersphere.dao.caretaker.CaretakerProfileDao;
import com.eldersphere.dao.file.FileAssetDao;
import com.eldersphere.dao.review.ReviewDao;
import com.eldersphere.dao.review.ReviewReplyDao;
import com.eldersphere.dao.user.UserDao;
import com.eldersphere.dtos.Review.ReviewReplyRequest;
import com.eldersphere.dtos.Review.ReviewReplyResponse;
import com.eldersphere.dtos.Review.ReviewRequest;
import com.eldersphere.dtos.Review.ReviewResponse;
import com.eldersphere.entities.Booking;
import com.eldersphere.entities.CaretakerProfile;
import com.eldersphere.entities.FileAsset;
import com.eldersphere.entities.Review;
import com.eldersphere.entities.ReviewReply;
import com.eldersphere.entities.User;
import com.eldersphere.enums.BookingStatusEnum;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.services.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewDao reviewDao;
    private final ReviewReplyDao reviewReplyDao;
    private final BookingDao bookingDao;
    private final CaretakerProfileDao caretakerProfileDao;
    private final UserDao userDao;
    private final FileAssetDao fileAssetDao;

    @Override
    @Transactional
    public ReviewResponse create(Long reviewerId, ReviewRequest request) throws GenericException {
        Booking booking = bookingDao.findById(request.getBookingId(), true);
        if (booking == null) {
            throw new GenericException(ExceptionCodeEnum.BOOKING_NOT_FOUND, "Booking not found");
        }
        if (booking.getStatus() != BookingStatusEnum.COMPLETED) {
            throw new GenericException(ExceptionCodeEnum.INVALID_BOOKING_STATUS_TRANSITION,
                    "Only completed bookings can be reviewed");
        }
        if (reviewDao.existsByBookingId(request.getBookingId())) {
            throw new GenericException(ExceptionCodeEnum.DUPLICATE_REVIEW, "This booking has already been reviewed");
        }

        Review review = Review.builder()
                .bookingId(request.getBookingId())
                .reviewerId(reviewerId)
                .caretakerId(booking.getCaretakerId())
                .rating(request.getRating())
                .comment(request.getComment())
                .punctualityRating(request.getPunctualityRating() != null ? request.getPunctualityRating() : request.getRating())
                .careQualityRating(request.getCareQualityRating() != null ? request.getCareQualityRating() : request.getRating())
                .communicationRating(request.getCommunicationRating() != null ? request.getCommunicationRating() : request.getRating())
                .photoFileAssetId(request.getPhotoFileAssetId())
                .build();
        review = reviewDao.save(review);

        // Recompute the caretaker's average rating.
        CaretakerProfile caretaker = caretakerProfileDao.findById(booking.getCaretakerId(), true);
        if (caretaker != null) {
            Double average = reviewDao.findAverageRatingByCaretakerId(booking.getCaretakerId());
            caretaker.setRatingAverage(average != null ? average : 0.0);
            caretakerProfileDao.save(caretaker);
        }

        return toResponse(review);
    }

    @Override
    public Page<ReviewResponse> getByCaretaker(Long caretakerId, Pageable pageable) {
        return reviewDao.findByCaretakerId(caretakerId, pageable).map(this::toResponse);
    }

    @Override
    public Page<ReviewResponse> getByReviewer(Long reviewerId, Pageable pageable) {
        return reviewDao.findByReviewerId(reviewerId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public ReviewReplyResponse upsertReply(Long callerUserId, Long reviewId, ReviewReplyRequest request) throws GenericException {
        Review review = reviewDao.findById(reviewId, true);
        if (review == null) {
            throw new GenericException(ExceptionCodeEnum.REVIEW_NOT_FOUND, "Review not found");
        }

        CaretakerProfile caretaker = caretakerProfileDao.findById(review.getCaretakerId(), true);
        if (caretaker == null || !caretaker.getUserId().equals(callerUserId)) {
            throw new GenericException(ExceptionCodeEnum.FORBIDDEN, "Only the reviewed caretaker can reply to this review");
        }

        ReviewReply reply = reviewReplyDao.findByReviewId(reviewId).orElse(null);
        if (reply == null) {
            reply = ReviewReply.builder()
                    .reviewId(reviewId)
                    .caretakerId(review.getCaretakerId())
                    .content(request.getContent())
                    .build();
        } else {
            reply.setContent(request.getContent());
        }
        reply = reviewReplyDao.save(reply);

        ReviewReplyResponse response = new ReviewReplyResponse();
        response.setId(reply.getId());
        response.setReviewId(reply.getReviewId());
        response.setCaretakerId(reply.getCaretakerId());
        response.setContent(reply.getContent());
        response.setCreatedAt(reply.getCreatedAt());
        response.setUpdatedAt(reply.getUpdatedAt());
        return response;
    }

    private ReviewResponse toResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setBookingId(review.getBookingId());
        response.setReviewerId(review.getReviewerId());
        User reviewer = userDao.findById(review.getReviewerId(), true);
        if (reviewer != null) response.setReviewerName(reviewer.getFullName());
        response.setCaretakerId(review.getCaretakerId());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setPunctualityRating(review.getPunctualityRating());
        response.setCareQualityRating(review.getCareQualityRating());
        response.setCommunicationRating(review.getCommunicationRating());
        response.setPhotoFileAssetId(review.getPhotoFileAssetId());
        if (review.getPhotoFileAssetId() != null) {
            FileAsset asset = fileAssetDao.findById(review.getPhotoFileAssetId(), true);
            if (asset != null) response.setPhotoUrl(asset.getUrl());
        }
        reviewReplyDao.findByReviewId(review.getId()).ifPresent(reply -> {
            ReviewReplyResponse replyResponse = new ReviewReplyResponse();
            replyResponse.setId(reply.getId());
            replyResponse.setReviewId(reply.getReviewId());
            replyResponse.setCaretakerId(reply.getCaretakerId());
            replyResponse.setContent(reply.getContent());
            replyResponse.setCreatedAt(reply.getCreatedAt());
            replyResponse.setUpdatedAt(reply.getUpdatedAt());
            response.setReply(replyResponse);
        });
        response.setCreatedAt(review.getCreatedAt());
        response.setUpdatedAt(review.getUpdatedAt());
        return response;
    }
}
