package com.eldersphere.dtos.Payment;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreatePaymentIntentRequest {
    @NotNull(message = "Booking is required")
    private Long bookingId;
}
