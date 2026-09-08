package com.eldersphere.dtos.Landing;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LandingFaqResponse {
    private Long id;
    private String question;
    private String answer;
    private Integer sortOrder;
    private boolean isActive;
}
