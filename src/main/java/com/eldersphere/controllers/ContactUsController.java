package com.eldersphere.controllers;

import com.eldersphere.dtos.ContactUs.ContactUsRequest;
import com.eldersphere.dtos.ContactUs.ContactUsResponse;
import com.eldersphere.enums.ContactUsStatusEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import com.eldersphere.services.ContactUsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/contact-us")
@Tag(name = "Contact Us", description = "Public contact form submissions and admin triage")
@RequiredArgsConstructor
public class ContactUsController {

    private final ContactUsService contactUsService;

    @Operation(summary = "Submit a contact request (public)")
    @PostMapping
    public ResponseEntity<ResponseModel<ContactUsResponse>> create(@Valid @RequestBody ContactUsRequest request) {
        return ApiResponse.createSuccess(contactUsService.create(request), "Thanks for reaching out, we'll get back to you soon");
    }

    @PreAuthorize("hasRole('ADMIN') and @adminPermissionGuard.has('CONTACT_US_VIEW')")
    @Operation(summary = "Search contact requests (admin)")
    @GetMapping
    public ResponseEntity<ResponseModel<Page<ContactUsResponse>>> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ContactUsStatusEnum status,
            Pageable pageable) {
        return ApiResponse.successResponse(contactUsService.search(search, status, pageable), "Contact requests fetched successfully");
    }

    @PreAuthorize("hasRole('ADMIN') and @adminPermissionGuard.has('CONTACT_US_MANAGE')")
    @Operation(summary = "Update contact request status (admin)")
    @PutMapping("/{id}/status")
    public ResponseEntity<ResponseModel<ContactUsResponse>> updateStatus(@PathVariable Long id, @RequestParam ContactUsStatusEnum status) throws GenericException {
        return ApiResponse.successResponse(contactUsService.updateStatus(id, status), "Status updated successfully");
    }

    @PreAuthorize("hasRole('ADMIN') and @adminPermissionGuard.has('CONTACT_US_MANAGE')")
    @Operation(summary = "Delete contact request (admin)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseModel<String>> delete(@PathVariable Long id) throws GenericException {
        contactUsService.delete(id);
        return ApiResponse.successResponse("Contact request deleted successfully");
    }
}
