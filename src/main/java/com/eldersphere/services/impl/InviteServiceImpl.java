package com.eldersphere.services.impl;

import com.eldersphere.dao.elder.ElderProfileDao;
import com.eldersphere.dao.elder.ElderProfileLinkInviteDao;
import com.eldersphere.dao.elder.FamilyElderLinkDao;
import com.eldersphere.dao.user.UserDao;
import com.eldersphere.dtos.Elder.CreateInviteRequest;
import com.eldersphere.dtos.Elder.InviteResponse;
import com.eldersphere.dtos.Elder.InviteSummaryDTO;
import com.eldersphere.entities.ElderProfile;
import com.eldersphere.entities.ElderProfileLinkInvite;
import com.eldersphere.entities.FamilyElderLink;
import com.eldersphere.entities.User;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.enums.LinkInviteStatusEnum;
import com.eldersphere.enums.NotificationTypeEnum;
import com.eldersphere.enums.UserTypeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.services.InviteService;
import com.eldersphere.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InviteServiceImpl implements InviteService {

    private static final Set<UserTypeEnum> ADMIN_TIER = EnumSet.of(UserTypeEnum.ADMIN, UserTypeEnum.SUPER_ADMIN);
    private static final Set<UserTypeEnum> INVITABLE_ROLES = EnumSet.of(UserTypeEnum.ELDER, UserTypeEnum.FAMILY_MEMBER);

    private final ElderProfileLinkInviteDao inviteDao;
    private final ElderProfileDao elderProfileDao;
    private final FamilyElderLinkDao familyElderLinkDao;
    private final UserDao userDao;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public InviteResponse createInvite(Long elderProfileId, CreateInviteRequest request, User caller) throws GenericException {
        ElderProfile profile = elderProfileDao.findById(elderProfileId, true);
        if (profile == null) {
            throw new GenericException(ExceptionCodeEnum.ELDER_PROFILE_NOT_FOUND, "Elder profile not found");
        }
        assertCanManage(profile, caller);

        if (!INVITABLE_ROLES.contains(request.getInvitedRole())) {
            throw new GenericException(ExceptionCodeEnum.INVALID_INVITE_ROLE, "invitedRole must be ELDER or FAMILY_MEMBER");
        }
        if (Objects.equals(request.getTargetUserId(), caller.getId())) {
            throw new GenericException(ExceptionCodeEnum.INVALID_INVITE_ROLE, "You cannot invite yourself");
        }
        User target = userDao.findById(request.getTargetUserId(), true);
        if (target == null) {
            throw new GenericException(ExceptionCodeEnum.USER_NOT_FOUND, "User not found");
        }
        if (target.getUserType() != request.getInvitedRole()) {
            throw new GenericException(ExceptionCodeEnum.INVALID_INVITE_ROLE,
                    "That user's account type does not match the requested role");
        }

        if (request.getInvitedRole() == UserTypeEnum.ELDER) {
            if (profile.getElderUserId() != null) {
                throw new GenericException(ExceptionCodeEnum.ELDER_ALREADY_LINKED, "This profile is already linked to an elder account");
            }
            if (elderProfileDao.findByElderUserId(target.getId()).isPresent()) {
                throw new GenericException(ExceptionCodeEnum.ELDER_ALREADY_LINKED, "That elder account is already linked to another profile");
            }
        } else {
            boolean alreadyLinked = Objects.equals(profile.getFamilyUserId(), target.getId())
                    || familyElderLinkDao.existsByElderProfileIdAndFamilyUserId(elderProfileId, target.getId());
            if (alreadyLinked) {
                throw new GenericException(ExceptionCodeEnum.FAMILY_MEMBER_ALREADY_LINKED, "That user already manages this elder profile");
            }
        }

        if (inviteDao.existsPendingForProfileAndUser(elderProfileId, target.getId())) {
            throw new GenericException(ExceptionCodeEnum.DUPLICATE_INVITE, "There is already a pending invite for that user");
        }

        ElderProfileLinkInvite invite = ElderProfileLinkInvite.builder()
                .elderProfileId(elderProfileId)
                .invitedUserId(target.getId())
                .invitedRole(request.getInvitedRole())
                .invitedByUserId(caller.getId())
                .relationshipLabel(request.getRelationshipLabel())
                .status(LinkInviteStatusEnum.PENDING)
                .build();
        invite = inviteDao.save(invite);

        String targetLink = request.getInvitedRole() == UserTypeEnum.ELDER ? "/elder/invites" : "/family/invites";
        notificationService.create(target.getId(), NotificationTypeEnum.ELDER_LINK_INVITE,
                "New family link request",
                caller.getFullName() + " invited you to link as " +
                        (request.getInvitedRole() == UserTypeEnum.ELDER ? "the elder" : "a family member") +
                        " for \"" + profile.getName() + "\"",
                targetLink);

        return toResponse(invite, profile);
    }

    @Override
    public List<InviteResponse> getInvitesForProfile(Long elderProfileId, User caller) throws GenericException {
        ElderProfile profile = elderProfileDao.findById(elderProfileId, true);
        if (profile == null) {
            throw new GenericException(ExceptionCodeEnum.ELDER_PROFILE_NOT_FOUND, "Elder profile not found");
        }
        assertCanManage(profile, caller);
        return inviteDao.findByElderProfileId(elderProfileId).stream()
                .map(invite -> toResponse(invite, profile))
                .collect(Collectors.toList());
    }

    @Override
    public List<InviteResponse> getMyInvites(Long userId) {
        return inviteDao.findPendingByInvitedUserId(userId).stream()
                .map(invite -> toResponse(invite, elderProfileDao.findById(invite.getElderProfileId(), true)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InviteResponse accept(Long inviteId, User caller) throws GenericException {
        ElderProfileLinkInvite invite = requirePendingInviteForCaller(inviteId, caller);
        ElderProfile profile = elderProfileDao.findById(invite.getElderProfileId(), true);
        if (profile == null) {
            throw new GenericException(ExceptionCodeEnum.ELDER_PROFILE_NOT_FOUND, "Elder profile not found");
        }

        if (invite.getInvitedRole() == UserTypeEnum.ELDER) {
            if (profile.getElderUserId() != null) {
                throw new GenericException(ExceptionCodeEnum.ELDER_ALREADY_LINKED, "This profile is already linked to an elder account");
            }
            if (elderProfileDao.findByElderUserId(caller.getId()).isPresent()) {
                throw new GenericException(ExceptionCodeEnum.ELDER_ALREADY_LINKED, "You are already linked to another elder profile");
            }
            profile.setElderUserId(caller.getId());
            elderProfileDao.save(profile);
        } else {
            if (familyElderLinkDao.existsByElderProfileIdAndFamilyUserId(profile.getId(), caller.getId())) {
                throw new GenericException(ExceptionCodeEnum.FAMILY_MEMBER_ALREADY_LINKED, "You already manage this elder profile");
            }
            familyElderLinkDao.save(FamilyElderLink.builder()
                    .elderProfileId(profile.getId())
                    .familyUserId(caller.getId())
                    .relationshipLabel(invite.getRelationshipLabel())
                    .invitedByUserId(invite.getInvitedByUserId())
                    .build());
        }

        invite.setStatus(LinkInviteStatusEnum.ACCEPTED);
        invite.setRespondedAt(LocalDateTime.now());
        invite = inviteDao.save(invite);

        notificationService.create(invite.getInvitedByUserId(), NotificationTypeEnum.ELDER_LINK_ACCEPTED,
                "Link request accepted",
                caller.getFullName() + " accepted your family link request for \"" + profile.getName() + "\"",
                "/family/elder-profiles");

        return toResponse(invite, profile);
    }

    @Override
    @Transactional
    public InviteResponse decline(Long inviteId, User caller) throws GenericException {
        ElderProfileLinkInvite invite = requirePendingInviteForCaller(inviteId, caller);
        ElderProfile profile = elderProfileDao.findById(invite.getElderProfileId(), true);

        invite.setStatus(LinkInviteStatusEnum.DECLINED);
        invite.setRespondedAt(LocalDateTime.now());
        invite = inviteDao.save(invite);

        notificationService.create(invite.getInvitedByUserId(), NotificationTypeEnum.ELDER_LINK_DECLINED,
                "Link request declined",
                caller.getFullName() + " declined your family link request" +
                        (profile != null ? " for \"" + profile.getName() + "\"" : ""),
                "/family/elder-profiles");

        return toResponse(invite, profile);
    }

    @Override
    @Transactional
    public void revoke(Long inviteId, User caller) throws GenericException {
        ElderProfileLinkInvite invite = inviteDao.findById(inviteId, true);
        if (invite == null) {
            throw new GenericException(ExceptionCodeEnum.ELDER_PROFILE_LINK_INVITE_NOT_FOUND, "Invite not found");
        }
        if (invite.getStatus() != LinkInviteStatusEnum.PENDING) {
            throw new GenericException(ExceptionCodeEnum.INVITE_ALREADY_RESPONDED, "This invite has already been responded to");
        }
        ElderProfile profile = elderProfileDao.findById(invite.getElderProfileId(), true);
        boolean isInviter = Objects.equals(invite.getInvitedByUserId(), caller.getId());
        boolean isProfileOwner = profile != null && Objects.equals(profile.getFamilyUserId(), caller.getId());
        if (!isInviter && !isProfileOwner && !ADMIN_TIER.contains(caller.getUserType())) {
            throw new GenericException(ExceptionCodeEnum.FORBIDDEN, "You cannot revoke this invite");
        }
        invite.setStatus(LinkInviteStatusEnum.REVOKED);
        invite.setRespondedAt(LocalDateTime.now());
        inviteDao.save(invite);
    }

    @Override
    public long countPendingForUser(Long userId) {
        return inviteDao.countPendingByInvitedUserId(userId);
    }

    @Override
    public List<InviteSummaryDTO> getPendingSummariesForUser(Long userId, int limit) {
        return inviteDao.findPendingByInvitedUserId(userId).stream()
                .limit(limit)
                .map(invite -> {
                    ElderProfile profile = elderProfileDao.findById(invite.getElderProfileId(), true);
                    User inviter = userDao.findById(invite.getInvitedByUserId(), true);
                    return InviteSummaryDTO.builder()
                            .id(invite.getId())
                            .elderProfileId(invite.getElderProfileId())
                            .elderName(profile != null ? profile.getName() : null)
                            .invitedByName(inviter != null ? inviter.getFullName() : null)
                            .relationshipLabel(invite.getRelationshipLabel())
                            .createdAt(invite.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private ElderProfileLinkInvite requirePendingInviteForCaller(Long inviteId, User caller) throws GenericException {
        ElderProfileLinkInvite invite = inviteDao.findById(inviteId, true);
        if (invite == null) {
            throw new GenericException(ExceptionCodeEnum.ELDER_PROFILE_LINK_INVITE_NOT_FOUND, "Invite not found");
        }
        if (!Objects.equals(invite.getInvitedUserId(), caller.getId())) {
            throw new GenericException(ExceptionCodeEnum.FORBIDDEN, "This invite is not addressed to you");
        }
        if (invite.getStatus() != LinkInviteStatusEnum.PENDING) {
            throw new GenericException(ExceptionCodeEnum.INVITE_ALREADY_RESPONDED, "This invite has already been responded to");
        }
        return invite;
    }

    private void assertCanManage(ElderProfile profile, User caller) throws GenericException {
        if (ADMIN_TIER.contains(caller.getUserType())) {
            return;
        }
        boolean isFamilyOwner = profile.getFamilyUserId() != null && Objects.equals(profile.getFamilyUserId(), caller.getId());
        boolean isElderOwner = profile.getElderUserId() != null && Objects.equals(profile.getElderUserId(), caller.getId());
        boolean isCoManagingFamily = familyElderLinkDao.existsByElderProfileIdAndFamilyUserId(profile.getId(), caller.getId());
        if (!isFamilyOwner && !isElderOwner && !isCoManagingFamily) {
            throw new GenericException(ExceptionCodeEnum.FORBIDDEN, "You do not have access to this elder profile");
        }
    }

    private InviteResponse toResponse(ElderProfileLinkInvite invite, ElderProfile profile) {
        InviteResponse response = new InviteResponse();
        response.setId(invite.getId());
        response.setElderProfileId(invite.getElderProfileId());
        response.setElderName(profile != null ? profile.getName() : null);
        response.setInvitedUserId(invite.getInvitedUserId());
        User invitedUser = userDao.findById(invite.getInvitedUserId(), true);
        response.setInvitedUserName(invitedUser != null ? invitedUser.getFullName() : null);
        response.setInvitedRole(invite.getInvitedRole());
        response.setInvitedByUserId(invite.getInvitedByUserId());
        User inviter = userDao.findById(invite.getInvitedByUserId(), true);
        response.setInvitedByName(inviter != null ? inviter.getFullName() : null);
        response.setRelationshipLabel(invite.getRelationshipLabel());
        response.setStatus(invite.getStatus());
        response.setRespondedAt(invite.getRespondedAt());
        response.setCreatedAt(invite.getCreatedAt());
        response.setUpdatedAt(invite.getUpdatedAt());
        return response;
    }
}
