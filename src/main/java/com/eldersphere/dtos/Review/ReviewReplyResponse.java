package com.eldersphere.dtos.Review;

import com.eldersphere.dtos.Common.AuditableResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReviewReplyResponse extends AuditableResponse {
    private Long id;
    private Long reviewId;
    private Long caretakerId;
    private String content;
}
