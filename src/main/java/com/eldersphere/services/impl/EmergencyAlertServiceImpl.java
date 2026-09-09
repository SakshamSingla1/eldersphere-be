package com.eldersphere.services.impl;

import com.eldersphere.dao.elder.ElderProfileDao;
import com.eldersphere.dao.emergency.EmergencyAlertDao;
import com.eldersphere.dtos.Emergency.EmergencyAlertRequest;
import com.eldersphere.dtos.Emergency.EmergencyAlertResponse;
import com.eldersphere.dtos.Emergency.EmergencyAlertUpdateRequest;
import com.eldersphere.entities.ElderProfile;
import com.eldersphere.entities.EmergencyAlert;
import com.eldersphere.enums.EmergencyAlertStatusEnum;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.enums.NotificationTypeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.services.EmergencyAlertService;
import com.eldersphere.services.GeocodingService;
import com.eldersphere.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmergencyAlertServiceImpl implements EmergencyAlertService {

    private final EmergencyAlertDao emergencyAlertDao;
    private final ElderProfileDao elderProfileDao;
    private final NotificationService notificationService;
    private final GeocodingService geocodingService;

    @Override
    @Transactional
    public EmergencyAlertResponse trigger(Long triggeredByUserId, EmergencyAlertRequest request) throws GenericException {
        ElderProfile elderProfile = elderProfileDao.findById(request.getElderProfileId(), true);
        if (elderProfile == null) {
            throw new GenericException(ExceptionCodeEnum.ELDER_PROFILE_NOT_FOUND, "Elder profile not found");
        }

        // Best-effort reverse geocoding (free OpenStreetMap Nominatim lookup, see
        // GeocodingServiceImpl) - a slow or failing geocoder must never block triggering the
        // alert itself, so this always degrades to a null resolvedAddress rather than throwing.
        String resolvedAddress = geocodingService.reverseGeocode(request.getLatitude(), request.getLongitude());

        EmergencyAlert alert = EmergencyAlert.builder()
                .elderProfileId(request.getElderProfileId())
                .triggeredByUserId(triggeredByUserId)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .resolvedAddress(resolvedAddress)
                .status(EmergencyAlertStatusEnum.TRIGGERED)
                .triggeredAt(LocalDateTime.now())
                .build();
        alert = emergencyAlertDao.save(alert);

        String locationSuffix = resolvedAddress != null ? " Last known location: " + resolvedAddress + "." : "";
        notificationService.create(elderProfile.getFamilyUserId(), NotificationTypeEnum.EMERGENCY_ALERT,
                "Emergency alert triggered",
                "An emergency alert was triggered for " + elderProfile.getName() + ". Please check on them immediately." + locationSuffix,
                null);

        return toResponse(alert, elderProfile);
    }

    @Override
    @Transactional
    public EmergencyAlertResponse updateStatus(Long id, EmergencyAlertUpdateRequest request) throws GenericException {
        EmergencyAlert alert = emergencyAlertDao.findById(id, true);
        if (alert == null) {
            throw new GenericException(ExceptionCodeEnum.EMERGENCY_ALERT_NOT_FOUND, "Emergency alert not found");
        }
        alert.setStatus(request.getStatus());
        if (request.getRespondingCaretakerId() != null) {
            alert.setRespondingCaretakerId(request.getRespondingCaretakerId());
        }
        if (request.getStatus() == EmergencyAlertStatusEnum.RESOLVED && alert.getResolvedAt() == null) {
            alert.setResolvedAt(LocalDateTime.now());
            if (alert.getTriggeredAt() != null) {
                alert.setResponseTimeSeconds((int) Duration.between(alert.getTriggeredAt(), alert.getResolvedAt()).getSeconds());
            }
        }
        alert = emergencyAlertDao.save(alert);
        ElderProfile elderProfile = elderProfileDao.findById(alert.getElderProfileId(), true);
        return toResponse(alert, elderProfile);
    }

    @Override
    public EmergencyAlertResponse getById(Long id) throws GenericException {
        EmergencyAlert alert = emergencyAlertDao.findById(id, true);
        if (alert == null) {
            throw new GenericException(ExceptionCodeEnum.EMERGENCY_ALERT_NOT_FOUND, "Emergency alert not found");
        }
        ElderProfile elderProfile = elderProfileDao.findById(alert.getElderProfileId(), true);
        return toResponse(alert, elderProfile);
    }

    @Override
    public Page<EmergencyAlertResponse> search(Long elderProfileId, EmergencyAlertStatusEnum status, Pageable pageable) {
        Page<EmergencyAlert> page;
        if (elderProfileId != null) {
            page = emergencyAlertDao.findByElderProfileId(elderProfileId, pageable);
        } else if (status != null) {
            page = emergencyAlertDao.findByStatus(status, pageable);
        } else {
            page = emergencyAlertDao.getRepository().findAll(pageable);
        }
        return page.map(alert -> toResponse(alert, elderProfileDao.findById(alert.getElderProfileId(), true)));
    }

    private EmergencyAlertResponse toResponse(EmergencyAlert alert, ElderProfile elderProfile) {
        EmergencyAlertResponse response = new EmergencyAlertResponse();
        response.setId(alert.getId());
        response.setElderProfileId(alert.getElderProfileId());
        if (elderProfile != null) response.setElderName(elderProfile.getName());
        response.setTriggeredByUserId(alert.getTriggeredByUserId());
        response.setLatitude(alert.getLatitude());
        response.setLongitude(alert.getLongitude());
        response.setResolvedAddress(alert.getResolvedAddress());
        response.setStatus(alert.getStatus());
        response.setRespondingCaretakerId(alert.getRespondingCaretakerId());
        response.setTriggeredAt(alert.getTriggeredAt());
        response.setResolvedAt(alert.getResolvedAt());
        response.setResponseTimeSeconds(alert.getResponseTimeSeconds());
        response.setCreatedAt(alert.getCreatedAt());
        response.setUpdatedAt(alert.getUpdatedAt());
        return response;
    }
}
