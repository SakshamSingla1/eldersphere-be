package com.eldersphere.services;

import com.eldersphere.dtos.Emergency.EmergencyAlertRequest;
import com.eldersphere.dtos.Emergency.EmergencyAlertResponse;
import com.eldersphere.dtos.Emergency.EmergencyAlertUpdateRequest;
import com.eldersphere.enums.EmergencyAlertStatusEnum;
import com.eldersphere.exceptions.GenericException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmergencyAlertService {
    EmergencyAlertResponse trigger(Long triggeredByUserId, EmergencyAlertRequest request) throws GenericException;
    EmergencyAlertResponse updateStatus(Long id, EmergencyAlertUpdateRequest request) throws GenericException;
    EmergencyAlertResponse getById(Long id) throws GenericException;
    Page<EmergencyAlertResponse> search(Long elderProfileId, EmergencyAlertStatusEnum status, Pageable pageable);
}
