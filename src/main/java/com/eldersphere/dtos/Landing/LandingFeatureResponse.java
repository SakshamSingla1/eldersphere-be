package com.eldersphere.dtos.Landing;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LandingFeatureResponse {
    private Long id;
    private String title;
    private String description;
    private String iconName;
    private Integer sortOrder;
    private boolean isActive;
}
