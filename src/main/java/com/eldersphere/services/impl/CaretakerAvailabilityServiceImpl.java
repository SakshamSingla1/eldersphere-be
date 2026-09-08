package com.eldersphere.services.impl;

import com.eldersphere.dao.caretaker.CaretakerAvailabilityDao;
import com.eldersphere.dao.caretaker.CaretakerProfileDao;
import com.eldersphere.dtos.Caretaker.AvailabilitySlotRequest;
import com.eldersphere.dtos.Caretaker.AvailabilitySlotResponse;
import com.eldersphere.dtos.Caretaker.CaretakerAvailabilityRequest;
import com.eldersphere.dtos.Caretaker.CaretakerAvailabilityResponse;
import com.eldersphere.entities.CaretakerAvailability;
import com.eldersphere.entities.CaretakerProfile;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.services.CaretakerAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CaretakerAvailabilityServiceImpl implements CaretakerAvailabilityService {

    private final CaretakerAvailabilityDao caretakerAvailabilityDao;
    private final CaretakerProfileDao caretakerProfileDao;

    @Override
    @Transactional
    public CaretakerAvailabilityResponse replaceForUser(Long userId, CaretakerAvailabilityRequest request) throws GenericException {
        CaretakerProfile profile = caretakerProfileDao.findByUserId(userId)
                .orElseThrow(() -> new GenericException(ExceptionCodeEnum.CARETAKER_PROFILE_NOT_FOUND, "Caretaker profile not found"));

        caretakerAvailabilityDao.deleteByCaretakerId(profile.getId());

        List<CaretakerAvailability> slots = request.getSlots().stream()
                .map(slot -> toEntity(profile.getId(), slot))
                .toList();
        if (!slots.isEmpty()) {
            caretakerAvailabilityDao.saveAll(slots);
        }

        return toResponse(profile.getId());
    }

    @Override
    public CaretakerAvailabilityResponse getByCaretakerId(Long caretakerId) throws GenericException {
        CaretakerProfile profile = caretakerProfileDao.findById(caretakerId, true);
        if (profile == null) {
            throw new GenericException(ExceptionCodeEnum.CARETAKER_PROFILE_NOT_FOUND, "Caretaker not found");
        }
        return toResponse(caretakerId);
    }

    private CaretakerAvailability toEntity(Long caretakerId, AvailabilitySlotRequest slot) {
        return CaretakerAvailability.builder()
                .caretakerId(caretakerId)
                .dayOfWeek(slot.getDayOfWeek())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .build();
    }

    private CaretakerAvailabilityResponse toResponse(Long caretakerId) {
        CaretakerAvailabilityResponse response = new CaretakerAvailabilityResponse();
        response.setCaretakerId(caretakerId);
        response.setSlots(caretakerAvailabilityDao.findByCaretakerId(caretakerId).stream()
                .map(this::toSlotResponse)
                .toList());
        return response;
    }

    private AvailabilitySlotResponse toSlotResponse(CaretakerAvailability slot) {
        AvailabilitySlotResponse response = new AvailabilitySlotResponse();
        response.setId(slot.getId());
        response.setDayOfWeek(slot.getDayOfWeek());
        response.setStartTime(slot.getStartTime());
        response.setEndTime(slot.getEndTime());
        return response;
    }
}
