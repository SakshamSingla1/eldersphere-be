package com.eldersphere.services.impl;

import com.eldersphere.dao.elder.ElderProfileDao;
import com.eldersphere.dtos.Elder.ElderProfileRequest;
import com.eldersphere.dtos.Elder.ElderProfileResponse;
import com.eldersphere.entities.ElderProfile;
import com.eldersphere.entities.User;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.enums.UserTypeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.services.ElderProfileService;
import com.eldersphere.utils.Helper;
import lombok.RequiredArgsConstructor;
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
        return elderProfileDao.findByFamilyUserId(familyUserId).stream()
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
    @Transactional
    public void delete(Long id) throws GenericException {
        ElderProfile profile = elderProfileDao.findById(id, true);
        if (profile == null) {
            throw new GenericException(ExceptionCodeEnum.ELDER_PROFILE_NOT_FOUND, "Elder profile not found");
        }
        assertCanAccess(profile);
        elderProfileDao.deleteById(id);
    }

    /**
     * Only the linked family_user_id owner, the linked elder_user_id owner,
     * or an ADMIN/SUPER_ADMIN may read or modify a given elder profile.
     */
    private void assertCanAccess(ElderProfile profile) throws GenericException {
        User caller = helper.getAuthenticatedUser();
        if (ADMIN_TIER.contains(caller.getUserType())) {
            return;
        }
        boolean isFamilyOwner = profile.getFamilyUserId() != null && Objects.equals(profile.getFamilyUserId(), caller.getId());
        boolean isElderOwner = profile.getElderUserId() != null && Objects.equals(profile.getElderUserId(), caller.getId());
        if (!isFamilyOwner && !isElderOwner) {
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
