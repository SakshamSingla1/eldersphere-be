package com.eldersphere.services;

import com.eldersphere.dtos.Elder.CreateInviteRequest;
import com.eldersphere.dtos.Elder.InviteResponse;
import com.eldersphere.dtos.Elder.InviteSummaryDTO;
import com.eldersphere.entities.User;
import com.eldersphere.exceptions.GenericException;

import java.util.List;

public interface InviteService {
    InviteResponse createInvite(Long elderProfileId, CreateInviteRequest request, User caller) throws GenericException;

    List<InviteResponse> getInvitesForProfile(Long elderProfileId, User caller) throws GenericException;

    List<InviteResponse> getMyInvites(Long userId);

    InviteResponse accept(Long inviteId, User caller) throws GenericException;

    InviteResponse decline(Long inviteId, User caller) throws GenericException;

    void revoke(Long inviteId, User caller) throws GenericException;

    long countPendingForUser(Long userId);

    List<InviteSummaryDTO> getPendingSummariesForUser(Long userId, int limit);
}
