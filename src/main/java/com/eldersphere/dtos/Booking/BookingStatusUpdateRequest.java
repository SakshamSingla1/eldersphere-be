package com.eldersphere.dtos.Booking;

import com.eldersphere.enums.BookingStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingStatusUpdateRequest {
    @NotNull(message = "Status is required")
    private BookingStatusEnum status;
}
