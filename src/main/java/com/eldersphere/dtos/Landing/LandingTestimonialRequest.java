package com.eldersphere.dtos.Landing;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LandingTestimonialRequest {
    @NotBlank(message = "Author name is required")
    private String authorName;
    private String authorRole;

    @NotBlank(message = "Content is required")
    private String content;

    private String avatarUrl;
    private Integer rating;
    private Integer sortOrder;
    private boolean isActive;
}
