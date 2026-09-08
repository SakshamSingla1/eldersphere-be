package com.eldersphere.dtos.Landing;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LandingPageResponse {
    private LandingPageConfigResponse config;
    private List<LandingFeatureResponse> features;
    private List<LandingFaqResponse> faqs;
    private List<LandingTestimonialResponse> testimonials;
}
