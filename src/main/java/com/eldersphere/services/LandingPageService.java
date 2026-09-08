package com.eldersphere.services;

import com.eldersphere.dtos.Landing.*;
import com.eldersphere.exceptions.GenericException;

import java.util.List;

public interface LandingPageService {
    LandingPageResponse getPublicPage();

    LandingPageConfigResponse updateConfig(LandingPageConfigRequest request);

    LandingFeatureResponse createFeature(LandingFeatureRequest request);
    List<LandingFeatureResponse> getAllFeatures();
    LandingFeatureResponse updateFeature(Long id, LandingFeatureRequest request) throws GenericException;
    void deleteFeature(Long id) throws GenericException;

    LandingFaqResponse createFaq(LandingFaqRequest request);
    List<LandingFaqResponse> getAllFaqs();
    LandingFaqResponse updateFaq(Long id, LandingFaqRequest request) throws GenericException;
    void deleteFaq(Long id) throws GenericException;

    LandingTestimonialResponse createTestimonial(LandingTestimonialRequest request);
    List<LandingTestimonialResponse> getAllTestimonials();
    LandingTestimonialResponse updateTestimonial(Long id, LandingTestimonialRequest request) throws GenericException;
    void deleteTestimonial(Long id) throws GenericException;
}
