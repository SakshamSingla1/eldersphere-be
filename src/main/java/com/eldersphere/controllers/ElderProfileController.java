package com.eldersphere.controllers;

import com.eldersphere.dtos.Elder.CreateInviteRequest;
import com.eldersphere.dtos.Elder.ElderProfileRequest;
import com.eldersphere.dtos.Elder.ElderProfileResponse;
import com.eldersphere.dtos.Elder.FamilyMemberSummaryDTO;
import com.eldersphere.dtos.Elder.InviteResponse;
import com.eldersphere.entities.User;
import com.eldersphere.enums.UserTypeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import com.eldersphere.services.ElderProfileService;
import com.eldersphere.services.InviteService;
import com.eldersphere.utils.Helper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final InviteService inviteService;
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

    @Operation(summary = "Search elder profiles by name (admin)", description = "There's no admin \"list all\" endpoint for elder profiles, so admin pickers (Elder Profile Lookup, Medical Records) that need to find a profile by name instead of its numeric ID call this.")
    @PreAuthorize("isAuthenticated() and @adminPermissionGuard.has('ELDER_PROFILES_VIEW')")
    @GetMapping("/search")
    public ResponseEntity<ResponseModel<Page<ElderProfileResponse>>> search(
            @RequestParam String query, Pageable pageable) {
        return ApiResponse.successResponse(elderProfileService.searchByName(query, pageable), "Elder profiles fetched successfully");
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

    @Operation(summary = "Invite a user to link with this elder profile", description = "Caller must own or co-manage the profile. invitedRole ELDER links the target as the elder (elderUserId); FAMILY_MEMBER invites them as an additional co-managing family member. The target must accept before any link is created — see InviteController.")
    @PostMapping("/{id}/invites")
    public ResponseEntity<ResponseModel<InviteResponse>> createInvite(
            @PathVariable Long id,
            @Valid @RequestBody CreateInviteRequest request,
            @RequestHeader(value = "Authorization", required = false) String auth) throws GenericException {
        User caller = helper.getUserFromHeader(auth);
        return ApiResponse.createSuccess(inviteService.createInvite(id, request, caller), "Invite sent successfully");
    }

    @Operation(summary = "List invites sent for this elder profile")
    @GetMapping("/{id}/invites")
    public ResponseEntity<ResponseModel<List<InviteResponse>>> getInvites(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String auth) throws GenericException {
        User caller = helper.getUserFromHeader(auth);
        return ApiResponse.successResponse(inviteService.getInvitesForProfile(id, caller), "Invites fetched successfully");
    }

    @Operation(summary = "List family members linked to this elder profile", description = "Owner plus any accepted co-managing family members.")
    @GetMapping("/{id}/family-members")
    public ResponseEntity<ResponseModel<List<FamilyMemberSummaryDTO>>> getFamilyMembers(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String auth) throws GenericException {
        User caller = helper.getUserFromHeader(auth);
        return ApiResponse.successResponse(elderProfileService.getFamilyMembers(id, caller), "Family members fetched successfully");
    }

    @Operation(summary = "Remove a co-managing family member", description = "Owner-only. Does not remove the original profile owner.")
    @DeleteMapping("/{id}/family-members/{userId}")
    public ResponseEntity<ResponseModel<String>> removeFamilyMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            @RequestHeader(value = "Authorization", required = false) String auth) throws GenericException {
        User caller = helper.getUserFromHeader(auth);
        elderProfileService.removeFamilyMember(id, userId, caller);
        return ApiResponse.successResponse("Family member removed successfully");
    }
}
