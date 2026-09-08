package com.eldersphere.controllers;

import com.eldersphere.dtos.ColorTheme.ColorThemeRequestDTO;
import com.eldersphere.dtos.ColorTheme.ColorThemeResponseDTO;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import com.eldersphere.services.ColorThemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/color-themes")
@Tag(name = "Color Themes", description = "Catalog of dashboard color theme presets. Listing is open to any authenticated user (theme picker); curating the catalog is SUPER_ADMIN only.")
@RequiredArgsConstructor
public class ColorThemeController {

    private final ColorThemeService colorThemeService;

    @Operation(summary = "Create color theme (SUPER_ADMIN)", description = "Requires SUPER_ADMIN role. If isDefault is true, unsets whichever other theme currently holds it.")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<ResponseModel<ColorThemeResponseDTO>> create(@Valid @RequestBody ColorThemeRequestDTO request) throws GenericException {
        return ApiResponse.createSuccess(colorThemeService.create(request), "Color theme created successfully");
    }

    @Operation(summary = "Update color theme (SUPER_ADMIN)", description = "Requires SUPER_ADMIN role.")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseModel<ColorThemeResponseDTO>> update(@PathVariable Long id, @Valid @RequestBody ColorThemeRequestDTO request) throws GenericException {
        return ApiResponse.successResponse(colorThemeService.update(id, request), "Color theme updated successfully");
    }

    @Operation(summary = "Get color theme by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseModel<ColorThemeResponseDTO>> getById(@PathVariable Long id) throws GenericException {
        return ApiResponse.successResponse(colorThemeService.getById(id), "Color theme fetched successfully");
    }

    @Operation(summary = "List active color themes", description = "Every ACTIVE theme preset, ordered by id - powers the theme picker for any authenticated user.")
    @GetMapping
    public ResponseEntity<ResponseModel<List<ColorThemeResponseDTO>>> getAllActive() {
        return ApiResponse.successResponse(colorThemeService.getAllActive(), "Color themes fetched successfully");
    }

    @Operation(summary = "Delete color theme (SUPER_ADMIN)", description = "Requires SUPER_ADMIN role. Rejects deleting the current default theme. Any user whose active theme was this one falls back to the default automatically.")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseModel<String>> delete(@PathVariable Long id) throws GenericException {
        colorThemeService.delete(id);
        return ApiResponse.successResponse("Color theme deleted successfully");
    }
}
