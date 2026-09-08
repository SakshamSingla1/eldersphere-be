package com.eldersphere.controllers;

import com.eldersphere.dtos.MedicalRecord.MedicalRecordRequest;
import com.eldersphere.dtos.MedicalRecord.MedicalRecordResponse;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import com.eldersphere.services.MedicalRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/v1/medical-records")
@Tag(name = "Medical Records", description = "Prescriptions, treatment notes and lab reports for an elder, optionally shared with family")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @Operation(summary = "Create medical record")
    @PreAuthorize("isAuthenticated() and @adminPermissionGuard.has('MEDICAL_RECORDS_MANAGE')")
    @PostMapping
    public ResponseEntity<ResponseModel<MedicalRecordResponse>> create(@Valid @RequestBody MedicalRecordRequest request) throws GenericException {
        return ApiResponse.createSuccess(medicalRecordService.create(request), "Medical record created successfully");
    }

    @Operation(summary = "Update medical record")
    @PreAuthorize("isAuthenticated() and @adminPermissionGuard.has('MEDICAL_RECORDS_MANAGE')")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseModel<MedicalRecordResponse>> update(@PathVariable Long id, @Valid @RequestBody MedicalRecordRequest request) throws GenericException {
        return ApiResponse.successResponse(medicalRecordService.update(id, request), "Medical record updated successfully");
    }

    @Operation(summary = "Get medical record by ID")
    @PreAuthorize("isAuthenticated() and @adminPermissionGuard.has('MEDICAL_RECORDS_VIEW')")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseModel<MedicalRecordResponse>> getById(@PathVariable Long id) throws GenericException {
        return ApiResponse.successResponse(medicalRecordService.getById(id), "Medical record fetched successfully");
    }

    @Operation(summary = "List medical records for an elder", description = "Set sharedOnly=true to fetch only the records the caretaker/creator has marked visible to family.")
    @PreAuthorize("isAuthenticated() and @adminPermissionGuard.has('MEDICAL_RECORDS_VIEW')")
    @GetMapping("/elder/{elderProfileId}")
    public ResponseEntity<ResponseModel<Page<MedicalRecordResponse>>> getByElder(
            @PathVariable Long elderProfileId,
            @RequestParam(defaultValue = "false") boolean sharedOnly,
            Pageable pageable) {
        return ApiResponse.successResponse(medicalRecordService.getByElderProfile(elderProfileId, sharedOnly, pageable), "Medical records fetched successfully");
    }

    @Operation(summary = "Delete medical record")
    @PreAuthorize("isAuthenticated() and @adminPermissionGuard.has('MEDICAL_RECORDS_MANAGE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseModel<String>> delete(@PathVariable Long id) throws GenericException {
        medicalRecordService.delete(id);
        return ApiResponse.successResponse("Medical record deleted successfully");
    }
}
