package com.eldersphere.services;

import com.eldersphere.dtos.Payment.CreatePaymentIntentResponse;
import com.eldersphere.dtos.Payment.EarningsResponse;
import com.eldersphere.dtos.Payment.PaymentResponse;
import com.eldersphere.enums.PaymentStatusEnum;
import com.eldersphere.exceptions.GenericException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    CreatePaymentIntentResponse createIntent(Long bookingId, Long callerUserId) throws GenericException;

    /**
     * Verifies the Stripe signature itself (never trusts an unverified webhook) and applies
     * the resulting event. Does not throw for events referencing a Payment this service can't
     * find or that is already in a terminal state - Stripe redelivers events, so this must
     * stay idempotent and simply no-op rather than fail the delivery.
     */
    void handleWebhook(String payload, String signatureHeader) throws GenericException;

    PaymentResponse getById(Long id, Long callerUserId) throws GenericException;

    PaymentResponse getByBooking(Long bookingId, Long callerUserId) throws GenericException;

    Page<PaymentResponse> search(Long bookingId, Long familyUserId, PaymentStatusEnum status, Pageable pageable);

    EarningsResponse getMyEarnings(Long callerUserId) throws GenericException;

    EarningsResponse getCaretakerEarnings(Long caretakerId) throws GenericException;

    PaymentResponse refund(Long paymentId, Long adminUserId) throws GenericException;
}
