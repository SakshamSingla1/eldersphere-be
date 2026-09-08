package com.eldersphere.services;

import com.eldersphere.dtos.MedicalRecord.MedicalRecordRequest;
import com.eldersphere.dtos.MedicalRecord.MedicalRecordResponse;
import com.eldersphere.exceptions.GenericException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MedicalRecordService {
    MedicalRecordResponse create(MedicalRecordRequest request) throws GenericException;
    MedicalRecordResponse update(Long id, MedicalRecordRequest request) throws GenericException;
    MedicalRecordResponse getById(Long id) throws GenericException;
    Page<MedicalRecordResponse> getByElderProfile(Long elderProfileId, boolean sharedOnly, Pageable pageable);
    void delete(Long id) throws GenericException;
}
