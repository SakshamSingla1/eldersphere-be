package com.eldersphere.controllers;

import com.eldersphere.dtos.Caretaker.CaretakerAvailabilityRequest;
import com.eldersphere.dtos.Caretaker.CaretakerAvailabilityResponse;
import com.eldersphere.dtos.Caretaker.CaretakerProfileRequest;
import com.eldersphere.dtos.Caretaker.CaretakerProfileResponse;
import com.eldersphere.dtos.Caretaker.CaretakerVerificationDocumentResponse;
import com.eldersphere.dtos.Caretaker.CaretakerVerificationUpdateRequest;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import com.eldersphere.services.CaretakerAvailabilityService;
import com.eldersphere.services.CaretakerProfileService;
import com.eldersphere.utils.Helper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/caretakers")
@Tag(name = "Caretakers", description = "Caretaker profile management and admin verification")
@RequiredArgsConstructor
public class CaretakerController {

    private final CaretakerProfileService caretakerProfileService;
    private final CaretakerAvailabilityService caretakerAvailabilityService;
    private final Helper helper;

    @Operation(summary = "Create or update my caretaker profile", description = "Called by a logged-in CARETAKER user to create or update their own profile.")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me")
    public ResponseEntity<ResponseModel<CaretakerProfileResponse>> upsertMyProfile(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @Valid @RequestBody CaretakerProfileRequest request) throws GenericException {
        Long userId = helper.getUserIdFromHeader(auth);
        CaretakerProfileResponse response = caretakerProfileService.createOrUpdateForUser(userId, request);
        return ApiResponse.successResponse(response, "Caretaker profile saved successfully");
    }

    @Operation(summary = "Get my caretaker profile")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<ResponseModel<CaretakerProfileResponse>> getMyProfile(
            @RequestHeader(value = "Authorization", required = false) String auth) throws GenericException {
        Long userId = helper.getUserIdFromHeader(auth);
        return ApiResponse.successResponse(caretakerProfileService.getByUserId(userId), "Caretaker profile fetched successfully");
    }

    @Operation(summary = "Get caretaker profile by ID")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseModel<CaretakerProfileResponse>> getById(@PathVariable Long id) throws GenericException {
        return ApiResponse.successResponse(caretakerProfileService.getById(id), "Caretaker profile fetched successfully");
    }

    @Operation(summary = "Update caretaker verification status (admin)")
    @PreAuthorize("hasRole('ADMIN') and @adminPermissionGuard.has('CARETAKER_VERIFICATION_MANAGE')")
    @PutMapping("/{id}/verification")
    public ResponseEntity<ResponseModel<CaretakerProfileResponse>> updateVerification(
            @PathVariable Long id, @Valid @RequestBody CaretakerVerificationUpdateRequest request) throws GenericException {
        CaretakerProfileResponse response = caretakerProfileService.updateVerification(id, request);
        return ApiResponse.successResponse(response, "Verification status updated successfully");
    }

    @Operation(summary = "Upload a verification document", description = "Called by a logged-in CARETAKER to attach evidence (ID, certification, background-check document, etc.) backing their verification review. Stores the file the same way POST /files does and records it against their own caretaker profile.")
    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/me/verification-documents", consumes = "multipart/form-data")
    public ResponseEntity<ResponseModel<CaretakerVerificationDocumentResponse>> uploadVerificationDocument(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestParam("file") MultipartFile file) throws GenericException {
        Long userId = helper.getUserIdFromHeader(auth);
        CaretakerVerificationDocumentResponse response = caretakerProfileService.uploadVerificationDocument(userId, file);
        return ApiResponse.createSuccess(response, "Verification document uploaded successfully");
    }

    @Operation(summary = "List a caretaker's submitted verification documents (admin)", description = "Used alongside PUT /{id}/verification when an admin is reviewing a caretaker's verification status.")
    @PreAuthorize("hasRole('ADMIN') and @adminPermissionGuard.has('CARETAKER_VERIFICATION_VIEW')")
    @GetMapping("/{id}/verification-documents")
    public ResponseEntity<ResponseModel<List<CaretakerVerificationDocumentResponse>>> listVerificationDocuments(
            @PathVariable Long id) throws GenericException {
        return ApiResponse.successResponse(caretakerProfileService.listVerificationDocuments(id), "Verification documents fetched successfully");
    }

    @Operation(summary = "Replace my weekly availability", description = "Called by a logged-in CARETAKER user. The submitted list of slots replaces their entire weekly availability (send an empty list to clear it).")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me/availability")
    public ResponseEntity<ResponseModel<CaretakerAvailabilityResponse>> replaceMyAvailability(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @Valid @RequestBody CaretakerAvailabilityRequest request) throws GenericException {
        Long userId = helper.getUserIdFromHeader(auth);
        return ApiResponse.successResponse(caretakerAvailabilityService.replaceForUser(userId, request), "Availability updated successfully");
    }

    @Operation(summary = "Get a caretaker's weekly availability", description = "Public - used by family members browsing/booking a caretaker.")
    @GetMapping("/{id}/availability")
    public ResponseEntity<ResponseModel<CaretakerAvailabilityResponse>> getAvailability(@PathVariable Long id) throws GenericException {
        return ApiResponse.successResponse(caretakerAvailabilityService.getByCaretakerId(id), "Availability fetched successfully");
    }
}
