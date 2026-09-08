package com.eldersphere.dtos.Booking;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class BookingRequest {
    private Long familyUserId;

    @NotNull(message = "Elder profile is required")
    private Long elderProfileId;

    @NotNull(message = "Caretaker is required")
    private Long caretakerId;

    @NotNull(message = "Service is required")
    private Long serviceId;

    @NotNull(message = "Scheduled date is required")
    private LocalDate scheduledDate;

    @NotNull(message = "Scheduled time is required")
    private LocalTime scheduledTime;

    private String notes;

    /**
     * Optional recurrence: when true, creates {@code occurrences} bookings
     * one week apart (starting at scheduledDate), all sharing a new
     * recurring_group_id. Each occurrence is validated independently against
     * caretaker availability and existing bookings - see BookingServiceImpl.
     */
    private Boolean repeatWeekly;

    @Min(value = 2, message = "occurrences must be between 2 and 12")
    @Max(value = 12, message = "occurrences must be between 2 and 12")
    private Integer occurrences;
}
