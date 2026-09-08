package com.eldersphere.dtos.Caretaker;

import com.eldersphere.enums.DayOfWeekEnum;
import lombok.Data;

import java.time.LocalTime;

@Data
public class AvailabilitySlotResponse {
    private Long id;
    private DayOfWeekEnum dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
}
