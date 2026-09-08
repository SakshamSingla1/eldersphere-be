package com.eldersphere.dtos.Landing;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LandingPageConfigResponse {
    private Long id;
    private String heroHeadline;
    private String heroSubheadline;
    private String heroImageUrl;
    private String ctaHeadline;
    private String ctaDescription;
    private String ctaButtonText;
}
