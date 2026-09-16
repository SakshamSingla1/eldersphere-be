package com.eldersphere.dtos.Booking;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AvailableSlotsResponse {
    private Long caretakerId;
    private LocalDate date;
    private boolean usingDefaultHours;
    private List<AvailableSlotResponse> slots;
}
