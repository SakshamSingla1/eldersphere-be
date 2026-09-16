package com.eldersphere.dtos.Booking;

import lombok.Data;

import java.time.LocalTime;

@Data
public class AvailableSlotResponse {
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean available;
}
