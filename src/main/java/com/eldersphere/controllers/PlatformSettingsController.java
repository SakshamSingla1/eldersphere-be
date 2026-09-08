package com.eldersphere.controllers;

import com.eldersphere.dtos.Platform.PlatformSettingsDTO;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import com.eldersphere.services.PlatformSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/platform-settings")
@Tag(name = "Platform Settings", description = "Singleton platform-wide configuration (name, support contacts, emergency SLA)")
@RequiredArgsConstructor
public class PlatformSettingsController {

    private final PlatformSettingsService platformSettingsService;

    @Operation(summary = "Get platform settings")
    @GetMapping
    public ResponseEntity<ResponseModel<PlatformSettingsDTO>> getSettings() {
        return ApiResponse.successResponse(platformSettingsService.getSettings(), "Platform settings fetched successfully");
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Update platform settings (super-admin)")
    @PutMapping
    public ResponseEntity<ResponseModel<PlatformSettingsDTO>> updateSettings(@Valid @RequestBody PlatformSettingsDTO dto) {
        return ApiResponse.successResponse(platformSettingsService.updateSettings(dto), "Platform settings updated successfully");
    }
}
