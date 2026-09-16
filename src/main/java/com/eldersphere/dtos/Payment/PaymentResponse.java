package com.eldersphere.dtos.Payment;

import com.eldersphere.dtos.Common.AuditableResponse;
import com.eldersphere.enums.PaymentStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentResponse extends AuditableResponse {
    private Long id;
    private Long bookingId;
    private Long familyUserId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatusEnum status;
    private String stripePaymentIntentId;
    private String stripeChargeId;
    private String failureReason;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
}
