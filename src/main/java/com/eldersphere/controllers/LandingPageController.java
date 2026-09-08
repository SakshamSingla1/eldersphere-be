package com.eldersphere.controllers;

import com.eldersphere.dtos.Landing.*;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import com.eldersphere.services.LandingPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/landing")
@Tag(name = "Landing Page", description = "Public landing page content and admin-editable sections (hero, features, FAQs, testimonials)")
@RequiredArgsConstructor
public class LandingPageController {

    private final LandingPageService landingPageService;

    @Operation(summary = "Get public landing page", description = "Aggregated hero config, active features, FAQs and testimonials for the marketing homepage.")
    @GetMapping("/page")
    public ResponseEntity<ResponseModel<LandingPageResponse>> getPublicPage() {
        return ApiResponse.successResponse(landingPageService.getPublicPage(), "Landing page fetched successfully");
    }

    @PreAuthorize("hasRole('ADMIN') and @adminPermissionGuard.has('LANDING_MANAGEMENT_MANAGE')")
    @Operation(summary = "Update landing page hero/CTA config (admin)")
    @PutMapping("/config")
    public ResponseEntity<ResponseModel<LandingPageConfigResponse>> updateConfig(@Valid @RequestBody LandingPageConfigRequest request) {
        return ApiResponse.successResponse(landingPageService.updateConfig(request), "Landing page config updated successfully");
    }

    @PreAuthorize("hasRole('ADMIN') and @adminPermissionGuard.has('LANDING_MANAGEMENT_MANAGE')")
    @Operation(summary = "Create landing feature (admin)")
    @PostMapping("/features")
    public ResponseEntity<ResponseModel<LandingFeatureResponse>> createFeature(@Valid @RequestBody LandingFeatureRequest request) {
        return ApiResponse.createSuccess(landingPageService.createFeature(request), "Feature created successfully");
    }

    @PreAuthorize("hasRole('ADMIN') and @adminPermissionGuard.has('LANDING_MANAGEMENT_VIEW')")
    @Operation(summary = "List all landing features (admin)")
    @GetMapping("/features")
    public ResponseEntity<ResponseModel<List<LandingFeatureResponse>>> getAllFeatures() {
        return ApiResponse.successResponse(landingPageService.getAllFeatures(), "Features fetched successfully");
    }

    @PreAuthorize("hasRole('ADMIN') and @adminPermissionGuard.has('LANDING_MANAGEMENT_MANAGE')")
    @Operation(summary = "Update landing feature (admin)")
    @PutMapping("/features/{id}")
    public ResponseEntity<ResponseModel<LandingFeatureResponse>> updateFeature(@PathVariable Long id, @Valid @RequestBody LandingFeatureRequest request) throws GenericException {
        return ApiResponse.successResponse(landingPageService.updateFeature(id, request), "Feature updated successfully");
    }

    @PreAuthorize("hasRole('ADMIN') and @adminPermissionGuard.has('LANDING_MANAGEMENT_MANAGE')")
    @Operation(summary = "Delete landing feature (admin)")
    @DeleteMapping("/features/{id}")
    public ResponseEntity<ResponseModel<String>> deleteFeature(@PathVariable Long id) throws GenericException {
        landingPageService.deleteFeature(id);
        return ApiResponse.successResponse("Feature deleted successfully");
    }

    @PreAuthorize("hasRole('ADMIN') and @adminPermissionGuard.has('LANDING_MANAGEMENT_MANAGE')")
    @Operation(summary = "Create landing FAQ (admin)")
    @PostMapping("/faqs")
    public ResponseEntity<ResponseModel<LandingFaqResponse>> createFaq(@Valid @RequestBody LandingFaqRequest request) {
        return ApiResponse.createSuccess(landingPageService.createFaq(request), "FAQ created successfully");
    }

    @PreAuthorize("hasRole('ADMIN') and @adminPermissionGuard.has('LANDING_MANAGEMENT_VIEW')")
    @Operation(summary = "List all landing FAQs (admin)")
    @GetMapping("/faqs")
    public ResponseEntity<ResponseModel<List<LandingFaqResponse>>> getAllFaqs() {
        return ApiResponse.successResponse(landingPageService.getAllFaqs(), "FAQs fetched successfully");
    }

    @PreAuthorize("hasRole('ADMIN') and @adminPermissionGuard.has('LANDING_MANAGEMENT_MANAGE')")
    @Operation(summary = "Update landing FAQ (admin)")
    @PutMapping("/faqs/{id}")
    public ResponseEntity<ResponseModel<LandingFaqResponse>> updateFaq(@PathVariable Long id, @Valid @RequestBody LandingFaqRequest request) throws GenericException {
        return ApiResponse.successResponse(landingPageService.updateFaq(id, request), "FAQ updated successfully");
    }

    @PreAuthorize("hasRole('ADMIN') and @adminPermissionGuard.has('LANDING_MANAGEMENT_MANAGE')")
    @Operation(summary = "Delete landing FAQ (admin)")
    @DeleteMapping("/faqs/{id}")
    public ResponseEntity<ResponseModel<String>> deleteFaq(@PathVariable Long id) throws GenericException {
        landingPageService.deleteFaq(id);
        return ApiResponse.successResponse("FAQ deleted successfully");
    }

    @PreAuthorize("hasRole('ADMIN') and @adminPermissionGuard.has('LANDING_MANAGEMENT_MANAGE')")
    @Operation(summary = "Create landing testimonial (admin)")
    @PostMapping("/testimonials")
    public ResponseEntity<ResponseModel<LandingTestimonialResponse>> createTestimonial(@Valid @RequestBody LandingTestimonialRequest request) {
        return ApiResponse.createSuccess(landingPageService.createTestimonial(request), "Testimonial created successfully");
    }

    @PreAuthorize("hasRole('ADMIN') and @adminPermissionGuard.has('LANDING_MANAGEMENT_VIEW')")
    @Operation(summary = "List all landing testimonials (admin)")
    @GetMapping("/testimonials")
    public ResponseEntity<ResponseModel<List<LandingTestimonialResponse>>> getAllTestimonials() {
        return ApiResponse.successResponse(landingPageService.getAllTestimonials(), "Testimonials fetched successfully");
    }

    @PreAuthorize("hasRole('ADMIN') and @adminPermissionGuard.has('LANDING_MANAGEMENT_MANAGE')")
    @Operation(summary = "Update landing testimonial (admin)")
    @PutMapping("/testimonials/{id}")
    public ResponseEntity<ResponseModel<LandingTestimonialResponse>> updateTestimonial(@PathVariable Long id, @Valid @RequestBody LandingTestimonialRequest request) throws GenericException {
        return ApiResponse.successResponse(landingPageService.updateTestimonial(id, request), "Testimonial updated successfully");
    }

    @PreAuthorize("hasRole('ADMIN') and @adminPermissionGuard.has('LANDING_MANAGEMENT_MANAGE')")
    @Operation(summary = "Delete landing testimonial (admin)")
    @DeleteMapping("/testimonials/{id}")
    public ResponseEntity<ResponseModel<String>> deleteTestimonial(@PathVariable Long id) throws GenericException {
        landingPageService.deleteTestimonial(id);
        return ApiResponse.successResponse("Testimonial deleted successfully");
    }
}
