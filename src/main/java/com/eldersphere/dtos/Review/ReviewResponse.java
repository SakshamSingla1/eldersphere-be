package com.eldersphere.dtos.Review;

import com.eldersphere.dtos.Common.AuditableResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReviewResponse extends AuditableResponse {
    private Long id;
    private Long bookingId;
    private Long reviewerId;
    private String reviewerName;
    private Long caretakerId;
    private Integer rating;
    private String comment;
    private Integer punctualityRating;
    private Integer careQualityRating;
    private Integer communicationRating;
    private Long photoFileAssetId;
    private String photoUrl;
    private ReviewReplyResponse reply;
}
