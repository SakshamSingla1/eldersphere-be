package com.eldersphere.dtos.Caretaker;

import lombok.Data;

import java.util.List;

@Data
public class CaretakerAvailabilityResponse {
    private Long caretakerId;
    private List<AvailabilitySlotResponse> slots;
}
