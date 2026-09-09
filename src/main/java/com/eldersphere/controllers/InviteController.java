package com.eldersphere.controllers;

import com.eldersphere.dtos.Elder.InviteResponse;
import com.eldersphere.entities.User;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import com.eldersphere.services.InviteService;
import com.eldersphere.utils.Helper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/v1/invites")
@Tag(name = "Invites", description = "Family-elder link invites: the invited user's own inbox, and accept/decline/revoke actions")
@RequiredArgsConstructor
public class InviteController {

    private final InviteService inviteService;
    private final Helper helper;

    @Operation(summary = "List my pending invites", description = "Invites addressed to the logged-in user, whether they were invited as an ELDER or as a co-managing FAMILY_MEMBER.")
    @GetMapping("/me")
    public ResponseEntity<ResponseModel<List<InviteResponse>>> getMyInvites(
            @RequestHeader(value = "Authorization", required = false) String auth) throws GenericException {
        Long userId = helper.getUserIdFromHeader(auth);
        return ApiResponse.successResponse(inviteService.getMyInvites(userId), "Invites fetched successfully");
    }

    @Operation(summary = "Accept an invite")
    @PutMapping("/{id}/accept")
    public ResponseEntity<ResponseModel<InviteResponse>> accept(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String auth) throws GenericException {
        User caller = helper.getUserFromHeader(auth);
        return ApiResponse.successResponse(inviteService.accept(id, caller), "Invite accepted successfully");
    }

    @Operation(summary = "Decline an invite")
    @PutMapping("/{id}/decline")
    public ResponseEntity<ResponseModel<InviteResponse>> decline(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String auth) throws GenericException {
        User caller = helper.getUserFromHeader(auth);
        return ApiResponse.successResponse(inviteService.decline(id, caller), "Invite declined successfully");
    }

    @Operation(summary = "Revoke a pending invite", description = "Only the inviter or the elder profile's owner may revoke.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseModel<String>> revoke(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String auth) throws GenericException {
        User caller = helper.getUserFromHeader(auth);
        inviteService.revoke(id, caller);
        return ApiResponse.successResponse("Invite revoked successfully");
    }
}
