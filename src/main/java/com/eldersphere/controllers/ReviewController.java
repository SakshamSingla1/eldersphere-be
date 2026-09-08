package com.eldersphere.controllers;

import com.eldersphere.dtos.Review.ReviewReplyRequest;
import com.eldersphere.dtos.Review.ReviewReplyResponse;
import com.eldersphere.dtos.Review.ReviewRequest;
import com.eldersphere.dtos.Review.ReviewResponse;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import com.eldersphere.services.ReviewService;
import com.eldersphere.utils.Helper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@Tag(name = "Reviews", description = "Family members rate and review caretakers after completed bookings")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final Helper helper;

    @Operation(summary = "Create review", description = "Only allowed for completed bookings, one review per booking.")
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<ResponseModel<ReviewResponse>> create(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @Valid @RequestBody ReviewRequest request) throws GenericException {
        Long reviewerId = helper.getUserIdFromHeader(auth);
        return ApiResponse.createSuccess(reviewService.create(reviewerId, request), "Review submitted successfully");
    }

    @Operation(summary = "Get reviews for a caretaker")
    @GetMapping("/caretaker/{caretakerId}")
    public ResponseEntity<ResponseModel<Page<ReviewResponse>>> getByCaretaker(@PathVariable Long caretakerId, Pageable pageable) {
        return ApiResponse.successResponse(reviewService.getByCaretaker(caretakerId, pageable), "Reviews fetched successfully");
    }

    @Operation(summary = "Post or update my reply to a review", description = "Only the caretaker who was reviewed may reply, one reply per review.")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{reviewId}/reply")
    public ResponseEntity<ResponseModel<ReviewReplyResponse>> upsertReply(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long reviewId, @Valid @RequestBody ReviewReplyRequest request) throws GenericException {
        Long callerUserId = helper.getUserIdFromHeader(auth);
        return ApiResponse.successResponse(reviewService.upsertReply(callerUserId, reviewId, request), "Reply saved successfully");
    }
}
