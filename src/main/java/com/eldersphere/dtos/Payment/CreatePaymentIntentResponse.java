package com.eldersphere.dtos.Payment;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CreatePaymentIntentResponse {
    private Long paymentId;
    private String clientSecret;
    private BigDecimal amount;
    private String currency;
}
