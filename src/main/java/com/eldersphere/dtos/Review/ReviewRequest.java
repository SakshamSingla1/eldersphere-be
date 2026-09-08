package com.eldersphere.dtos.Review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewRequest {
    @NotNull(message = "Booking is required")
    private Long bookingId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    private String comment;

    /** Optional rating breakdown - defaults to the overall {@link #rating} when omitted. */
    @Min(value = 1, message = "Punctuality rating must be at least 1")
    @Max(value = 5, message = "Punctuality rating must be at most 5")
    private Integer punctualityRating;

    @Min(value = 1, message = "Care quality rating must be at least 1")
    @Max(value = 5, message = "Care quality rating must be at most 5")
    private Integer careQualityRating;

    @Min(value = 1, message = "Communication rating must be at least 1")
    @Max(value = 5, message = "Communication rating must be at most 5")
    private Integer communicationRating;

    /** Optional photo attached to the review, previously uploaded via /files. */
    private Long photoFileAssetId;
}
