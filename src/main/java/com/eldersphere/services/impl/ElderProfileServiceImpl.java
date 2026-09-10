package com.eldersphere.services.impl;

import com.eldersphere.dao.elder.ElderProfileDao;
import com.eldersphere.dao.elder.FamilyElderLinkDao;
import com.eldersphere.dao.user.UserDao;
import com.eldersphere.dtos.Elder.ElderProfileRequest;
import com.eldersphere.dtos.Elder.ElderProfileResponse;
import com.eldersphere.dtos.Elder.FamilyMemberSummaryDTO;
import com.eldersphere.entities.ElderProfile;
import com.eldersphere.entities.FamilyElderLink;
import com.eldersphere.entities.User;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.enums.UserTypeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.services.ElderProfileService;
import com.eldersphere.utils.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ElderProfileServiceImpl implements ElderProfileService {

    private static final Set<UserTypeEnum> ADMIN_TIER = EnumSet.of(UserTypeEnum.ADMIN, UserTypeEnum.SUPER_ADMIN);

    private final ElderProfileDao elderProfileDao;
    private final FamilyElderLinkDao familyElderLinkDao;
    private final UserDao userDao;
    private final Helper helper;

    @Override
    @Transactional
    public ElderProfileResponse create(ElderProfileRequest request) throws GenericException {
        ElderProfile profile = ElderProfile.builder()
                .familyUserId(request.getFamilyUserId())
                .elderUserId(request.getElderUserId())
                .name(request.getName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .medicalConditions(request.getMedicalConditions())
                .address(request.getAddress())
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactPhone(request.getEmergencyContactPhone())
                .build();
        return toResponse(elderProfileDao.save(profile));
    }

    @Override
    @Transactional
    public ElderProfileResponse update(Long id, ElderProfileRequest request) throws GenericException {
        ElderProfile profile = elderProfileDao.findById(id, true);
        if (profile == null) {
            throw new GenericException(ExceptionCodeEnum.ELDER_PROFILE_NOT_FOUND, "Elder profile not found");
        }
        assertCanAccess(profile);
        profile.setName(request.getName());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setGender(request.getGender());
        profile.setMedicalConditions(request.getMedicalConditions());
        profile.setAddress(request.getAddress());
        profile.setEmergencyContactName(request.getEmergencyContactName());
        profile.setEmergencyContactPhone(request.getEmergencyContactPhone());
        return toResponse(elderProfileDao.save(profile));
    }

    @Override
    public ElderProfileResponse getById(Long id) throws GenericException {
        ElderProfile profile = elderProfileDao.findById(id, true);
        if (profile == null) {
            throw new GenericException(ExceptionCodeEnum.ELDER_PROFILE_NOT_FOUND, "Elder profile not found");
        }
        assertCanAccess(profile);
        return toResponse(profile);
    }

    @Override
    public List<ElderProfileResponse> getByFamilyUserId(Long familyUserId) {
        return elderProfileDao.findOwnedOrCoManagedByFamilyUserId(familyUserId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ElderProfileResponse getByElderUserId(Long elderUserId) throws GenericException {
        ElderProfile profile = elderProfileDao.findByElderUserId(elderUserId)
                .orElseThrow(() -> new GenericException(ExceptionCodeEnum.ELDER_PROFILE_NOT_FOUND, "Elder profile not found"));
        return toResponse(profile);
    }

    @Override
    public Page<ElderProfileResponse> searchByName(String query, Pageable pageable) {
        return elderProfileDao.searchByName(query, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public void delete(Long id) throws GenericException {
        ElderProfile profile = elderProfileDao.findById(id, true);
        if (profile == null) {
            throw new GenericException(ExceptionCodeEnum.ELDER_PROFILE_NOT_FOUND, "Elder profile not found");
        }
        assertCanAccess(profile);
        elderProfileDao.deleteById(id);
    }

    @Override
    public List<FamilyMemberSummaryDTO> getFamilyMembers(Long elderProfileId, User caller) throws GenericException {
        ElderProfile profile = elderProfileDao.findById(elderProfileId, true);
        if (profile == null) {
            throw new GenericException(ExceptionCodeEnum.ELDER_PROFILE_NOT_FOUND, "Elder profile not found");
        }
        assertCanAccess(profile, caller);

        List<FamilyMemberSummaryDTO> members = new java.util.ArrayList<>();
        if (profile.getFamilyUserId() != null) {
            User owner = userDao.findById(profile.getFamilyUserId(), true);
            members.add(FamilyMemberSummaryDTO.builder()
                    .userId(profile.getFamilyUserId())
                    .fullName(owner != null ? owner.getFullName() : null)
                    .isOwner(true)
                    .build());
        }
        for (FamilyElderLink link : familyElderLinkDao.findByElderProfileId(elderProfileId)) {
            User member = userDao.findById(link.getFamilyUserId(), true);
            members.add(FamilyMemberSummaryDTO.builder()
                    .userId(link.getFamilyUserId())
                    .fullName(member != null ? member.getFullName() : null)
                    .relationshipLabel(link.getRelationshipLabel())
                    .isOwner(false)
                    .build());
        }
        return members;
    }

    @Override
    @Transactional
    public void removeFamilyMember(Long elderProfileId, Long targetUserId, User caller) throws GenericException {
        ElderProfile profile = elderProfileDao.findById(elderProfileId, true);
        if (profile == null) {
            throw new GenericException(ExceptionCodeEnum.ELDER_PROFILE_NOT_FOUND, "Elder profile not found");
        }
        boolean isOwner = profile.getFamilyUserId() != null && Objects.equals(profile.getFamilyUserId(), caller.getId());
        if (!isOwner && !ADMIN_TIER.contains(caller.getUserType())) {
            throw new GenericException(ExceptionCodeEnum.FORBIDDEN, "Only the profile owner can remove a family member");
        }
        if (!familyElderLinkDao.existsByElderProfileIdAndFamilyUserId(elderProfileId, targetUserId)) {
            throw new GenericException(ExceptionCodeEnum.DATA_NOT_FOUND, "That user is not a linked family member of this profile");
        }
        familyElderLinkDao.deleteByElderProfileIdAndFamilyUserId(elderProfileId, targetUserId);
    }

    /**
     * Only the linked family_user_id owner, an accepted co-managing family member (see
     * FamilyElderLink), the linked elder_user_id owner, or an ADMIN/SUPER_ADMIN may read or
     * modify a given elder profile.
     */
    private void assertCanAccess(ElderProfile profile) throws GenericException {
        assertCanAccess(profile, helper.getAuthenticatedUser());
    }

    private void assertCanAccess(ElderProfile profile, User caller) throws GenericException {
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

    private ElderProfileResponse toResponse(ElderProfile profile) {
        ElderProfileResponse response = new ElderProfileResponse();
        response.setId(profile.getId());
        response.setFamilyUserId(profile.getFamilyUserId());
        response.setElderUserId(profile.getElderUserId());
        response.setName(profile.getName());
        response.setDateOfBirth(profile.getDateOfBirth());
        response.setGender(profile.getGender());
        response.setMedicalConditions(profile.getMedicalConditions());
        response.setAddress(profile.getAddress());
        response.setEmergencyContactName(profile.getEmergencyContactName());
        response.setEmergencyContactPhone(profile.getEmergencyContactPhone());
        response.setCreatedAt(profile.getCreatedAt());
        response.setUpdatedAt(profile.getUpdatedAt());
        return response;
    }
}
