package com.eldersphere.controllers;

import com.eldersphere.dtos.Elder.ElderProfileRequest;
import com.eldersphere.dtos.Elder.ElderProfileResponse;
import com.eldersphere.entities.User;
import com.eldersphere.enums.UserTypeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import com.eldersphere.services.ElderProfileService;
import com.eldersphere.utils.Helper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/v1/elder-profiles")
@Tag(name = "Elder Profiles", description = "Elder profiles, either managed by a family member or self-managed by the elder")
@RequiredArgsConstructor
public class ElderProfileController {

    private static final Set<UserTypeEnum> ADMIN_TIER = EnumSet.of(UserTypeEnum.ADMIN, UserTypeEnum.SUPER_ADMIN);

    private final ElderProfileService elderProfileService;
    private final Helper helper;

    @Operation(summary = "Create elder profile", description = "Called by an ELDER user, creates a self-managed profile (elderUserId = caller). Called by a FAMILY_MEMBER, creates a family-managed profile (familyUserId = caller) as before.")
    @PostMapping
    public ResponseEntity<ResponseModel<ElderProfileResponse>> create(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @Valid @RequestBody ElderProfileRequest request) throws GenericException {
        User caller = helper.getUserFromHeader(auth);
        if (caller.getUserType() == UserTypeEnum.ELDER) {
            request.setElderUserId(caller.getId());
            request.setFamilyUserId(null);
        } else if (!ADMIN_TIER.contains(caller.getUserType())) {
            request.setFamilyUserId(caller.getId());
            request.setElderUserId(null);
        }
        return ApiResponse.createSuccess(elderProfileService.create(request), "Elder profile created successfully");
    }

    @Operation(summary = "Get my elder profile", description = "Called by a logged-in ELDER user to fetch their own self-managed profile.")
    @GetMapping("/me")
    public ResponseEntity<ResponseModel<ElderProfileResponse>> getMyProfile(
            @RequestHeader(value = "Authorization", required = false) String auth) throws GenericException {
        Long elderUserId = helper.getUserIdFromHeader(auth);
        return ApiResponse.successResponse(elderProfileService.getByElderUserId(elderUserId), "Elder profile fetched successfully");
    }

    @Operation(summary = "Update elder profile")
    @PreAuthorize("isAuthenticated() and @adminPermissionGuard.has('ELDER_PROFILES_MANAGE')")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseModel<ElderProfileResponse>> update(@PathVariable Long id, @Valid @RequestBody ElderProfileRequest request) throws GenericException {
        return ApiResponse.successResponse(elderProfileService.update(id, request), "Elder profile updated successfully");
    }

    @Operation(summary = "Get elder profile by ID")
    @PreAuthorize("isAuthenticated() and @adminPermissionGuard.has('ELDER_PROFILES_VIEW')")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseModel<ElderProfileResponse>> getById(@PathVariable Long id) throws GenericException {
        return ApiResponse.successResponse(elderProfileService.getById(id), "Elder profile fetched successfully");
    }

    @Operation(summary = "List my elders", description = "Lists all elder profiles managed by the logged-in family member.")
    @GetMapping
    public ResponseEntity<ResponseModel<List<ElderProfileResponse>>> getMine(
            @RequestHeader(value = "Authorization", required = false) String auth) throws GenericException {
        Long familyUserId = helper.getUserIdFromHeader(auth);
        return ApiResponse.successResponse(elderProfileService.getByFamilyUserId(familyUserId), "Elder profiles fetched successfully");
    }

    @Operation(summary = "Delete elder profile")
    @PreAuthorize("isAuthenticated() and @adminPermissionGuard.has('ELDER_PROFILES_MANAGE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseModel<String>> delete(@PathVariable Long id) throws GenericException {
        elderProfileService.delete(id);
        return ApiResponse.successResponse("Elder profile deleted successfully");
    }
}
