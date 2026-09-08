package com.eldersphere.services;

import com.eldersphere.dtos.Caretaker.CaretakerAvailabilityRequest;
import com.eldersphere.dtos.Caretaker.CaretakerAvailabilityResponse;
import com.eldersphere.exceptions.GenericException;

public interface CaretakerAvailabilityService {

    /** Replace-all: the submitted slots become the caretaker's entire weekly availability. */
    CaretakerAvailabilityResponse replaceForUser(Long userId, CaretakerAvailabilityRequest request) throws GenericException;

    CaretakerAvailabilityResponse getByCaretakerId(Long caretakerId) throws GenericException;
}
