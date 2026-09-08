package com.eldersphere.dtos.Landing;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LandingTestimonialResponse {
    private Long id;
    private String authorName;
    private String authorRole;
    private String content;
    private String avatarUrl;
    private Integer rating;
    private Integer sortOrder;
    private boolean isActive;
}
