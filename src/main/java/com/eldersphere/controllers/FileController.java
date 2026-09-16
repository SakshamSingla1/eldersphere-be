package com.eldersphere.controllers;

import com.eldersphere.dtos.File.FileAssetResponse;
import com.eldersphere.enums.ResourceTypeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import com.eldersphere.services.FileAssetService;
import com.eldersphere.utils.Helper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/v1/files")
@Tag(name = "Files", description = "File uploads (images, PDF, Word, Excel, video) - stored on Cloudinary when configured, otherwise on local disk served from /uploads/**")
@RequiredArgsConstructor
public class FileController {

    private final FileAssetService fileAssetService;
    private final Helper helper;

    @Operation(summary = "Upload a file", description = "Stores an image, PDF, Word, Excel, or video file (Cloudinary when configured, otherwise local disk) and returns its public URL and metadata.")
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ResponseModel<FileAssetResponse>> upload(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestParam("file") MultipartFile file,
            @RequestParam ResourceTypeEnum resourceType) throws GenericException {
        Long userId = helper.getUserIdFromHeader(auth);
        FileAssetResponse response = fileAssetService.upload(file, resourceType, userId);
        return ApiResponse.createSuccess(response, "File uploaded successfully");
    }

    @Operation(summary = "Get file metadata by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseModel<FileAssetResponse>> getById(@PathVariable Long id) throws GenericException {
        return ApiResponse.successResponse(fileAssetService.getById(id), "File fetched successfully");
    }

    @Operation(summary = "Delete a file")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseModel<String>> delete(@PathVariable Long id) throws GenericException {
        fileAssetService.delete(id);
        return ApiResponse.successResponse("File deleted successfully");
    }
}
