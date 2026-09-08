package com.eldersphere.dtos.Review;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReviewReplyRequest {

    @NotBlank(message = "Reply content is required")
    private String content;
}
