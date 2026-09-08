package com.eldersphere.dtos.Landing;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LandingFeatureRequest {
    @NotBlank(message = "Title is required")
    private String title;
    private String description;
    private String iconName;
    private Integer sortOrder;
    private boolean isActive;
}
