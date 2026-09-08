package com.eldersphere.services;

import com.eldersphere.dtos.Review.ReviewReplyRequest;
import com.eldersphere.dtos.Review.ReviewReplyResponse;
import com.eldersphere.dtos.Review.ReviewRequest;
import com.eldersphere.dtos.Review.ReviewResponse;
import com.eldersphere.exceptions.GenericException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewResponse create(Long reviewerId, ReviewRequest request) throws GenericException;
    Page<ReviewResponse> getByCaretaker(Long caretakerId, Pageable pageable);

    /** Reviews the given user wrote (as the reviewing family member), for account data export. */
    Page<ReviewResponse> getByReviewer(Long reviewerId, Pageable pageable);

    /** Create-or-update the one reply the reviewed caretaker may post on their review. */
    ReviewReplyResponse upsertReply(Long callerUserId, Long reviewId, ReviewReplyRequest request) throws GenericException;
}
