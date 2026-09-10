package com.eldersphere.services;

import com.eldersphere.dtos.Elder.ElderProfileRequest;
import com.eldersphere.dtos.Elder.ElderProfileResponse;
import com.eldersphere.dtos.Elder.FamilyMemberSummaryDTO;
import com.eldersphere.entities.User;
import com.eldersphere.exceptions.GenericException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ElderProfileService {
    ElderProfileResponse create(ElderProfileRequest request) throws GenericException;
    ElderProfileResponse update(Long id, ElderProfileRequest request) throws GenericException;
    ElderProfileResponse getById(Long id) throws GenericException;
    List<ElderProfileResponse> getByFamilyUserId(Long familyUserId);
    ElderProfileResponse getByElderUserId(Long elderUserId) throws GenericException;
    void delete(Long id) throws GenericException;
    Page<ElderProfileResponse> searchByName(String query, Pageable pageable);

    List<FamilyMemberSummaryDTO> getFamilyMembers(Long elderProfileId, User caller) throws GenericException;
    void removeFamilyMember(Long elderProfileId, Long targetUserId, User caller) throws GenericException;
}
