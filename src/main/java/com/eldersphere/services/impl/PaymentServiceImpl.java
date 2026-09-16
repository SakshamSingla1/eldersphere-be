package com.eldersphere.services.impl;

import com.eldersphere.config.StripeConfig;
import com.eldersphere.dao.booking.BookingDao;
import com.eldersphere.dao.caretaker.CaretakerProfileDao;
import com.eldersphere.dao.payment.PaymentDao;
import com.eldersphere.dao.user.UserDao;
import com.eldersphere.dtos.Payment.CreatePaymentIntentResponse;
import com.eldersphere.dtos.Payment.EarningsResponse;
import com.eldersphere.dtos.Payment.PaymentResponse;
import com.eldersphere.entities.Booking;
import com.eldersphere.entities.CaretakerProfile;
import com.eldersphere.entities.Payment;
import com.eldersphere.entities.User;
import com.eldersphere.enums.BookingStatusEnum;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.enums.NotificationTypeEnum;
import com.eldersphere.enums.PaymentStatusEnum;
import com.eldersphere.enums.UserTypeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.services.NotificationService;
import com.eldersphere.services.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final Set<UserTypeEnum> ADMIN_TIER = EnumSet.of(UserTypeEnum.ADMIN, UserTypeEnum.SUPER_ADMIN);

    private final PaymentDao paymentDao;
    private final BookingDao bookingDao;
    private final CaretakerProfileDao caretakerProfileDao;
    private final UserDao userDao;
    private final NotificationService notificationService;
    private final StripeConfig stripeConfig;

    @Override
    @Transactional
    public CreatePaymentIntentResponse createIntent(Long bookingId, Long callerUserId) throws GenericException {
        Booking booking = bookingDao.findById(bookingId, true);
        if (booking == null) {
            throw new GenericException(ExceptionCodeEnum.BOOKING_NOT_FOUND, "Booking not found");
        }
        if (!booking.getFamilyUserId().equals(callerUserId)) {
            throw new GenericException(ExceptionCodeEnum.FORBIDDEN, "You do not have access to pay for this booking");
        }
        if (booking.getStatus() != BookingStatusEnum.CONFIRMED) {
            throw new GenericException(ExceptionCodeEnum.BOOKING_NOT_PAYABLE, "Only a CONFIRMED booking can be paid for");
        }
        if (!stripeConfig.isConfigured()) {
            throw new GenericException(ExceptionCodeEnum.PAYMENT_GATEWAY_NOT_CONFIGURED, "Payments are not configured on this server");
        }

        Optional<Payment> existing = paymentDao.findNonTerminalByBookingId(bookingId);
        if (existing.isPresent()) {
            Payment payment = existing.get();
            String clientSecret = retrieveClientSecret(payment.getStripePaymentIntentId());
            return CreatePaymentIntentResponse.builder()
                    .paymentId(payment.getId())
                    .clientSecret(clientSecret)
                    .amount(payment.getAmount())
                    .currency(payment.getCurrency())
                    .build();
        }

        BigDecimal amount = booking.getCost();
        Payment payment = Payment.builder()
                .bookingId(bookingId)
                .familyUserId(callerUserId)
                .amount(amount)
                .currency("usd")
                .status(PaymentStatusEnum.PENDING)
                .build();
        payment = paymentDao.save(payment);

        PaymentIntent intent = createStripeIntent(bookingId, payment.getId(), amount);
        payment.setStripePaymentIntentId(intent.getId());
        payment = paymentDao.save(payment);

        return CreatePaymentIntentResponse.builder()
                .paymentId(payment.getId())
                .clientSecret(intent.getClientSecret())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .build();
    }

    private PaymentIntent createStripeIntent(Long bookingId, Long paymentId, BigDecimal amount) {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(toCents(amount))
                    .setCurrency("usd")
                    .putMetadata("bookingId", String.valueOf(bookingId))
                    .putMetadata("paymentId", String.valueOf(paymentId))
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build())
                    .build();
            return PaymentIntent.create(params);
        } catch (StripeException e) {
            throw new IllegalStateException("Failed to create Stripe PaymentIntent for booking " + bookingId, e);
        }
    }

    private String retrieveClientSecret(String stripePaymentIntentId) {
        try {
            return PaymentIntent.retrieve(stripePaymentIntentId).getClientSecret();
        } catch (StripeException e) {
            throw new IllegalStateException("Failed to retrieve Stripe PaymentIntent " + stripePaymentIntentId, e);
        }
    }

    private static long toCents(BigDecimal amount) {
        return amount.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    @Override
    @Transactional
    public void handleWebhook(String payload, String signatureHeader) throws GenericException {
        if (!stripeConfig.isWebhookConfigured()) {
            throw new GenericException(ExceptionCodeEnum.WEBHOOK_SIGNATURE_INVALID, "Stripe webhook secret is not configured");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, signatureHeader, stripeConfig.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            throw new GenericException(ExceptionCodeEnum.WEBHOOK_SIGNATURE_INVALID, "Invalid Stripe webhook signature");
        }

        PaymentIntent intent = extractPaymentIntent(event);
        if (intent == null) {
            log.warn("Stripe webhook event {} of type {} carried no PaymentIntent payload - ignoring", event.getId(), event.getType());
            return;
        }

        switch (event.getType()) {
            case "payment_intent.succeeded" -> handleSucceeded(intent);
            case "payment_intent.payment_failed" -> handleFailed(intent);
            default -> log.debug("Ignoring unhandled Stripe webhook event type {}", event.getType());
        }
    }

    private PaymentIntent extractPaymentIntent(Event event) {
        Optional<StripeObject> stripeObject = event.getDataObjectDeserializer().getObject();
        return stripeObject.filter(PaymentIntent.class::isInstance)
                .map(PaymentIntent.class::cast)
                .orElse(null);
    }

    private void handleSucceeded(PaymentIntent intent) {
        Payment payment = paymentDao.findByStripePaymentIntentId(intent.getId()).orElse(null);
        if (payment == null) {
            log.warn("Stripe payment_intent.succeeded for unknown PaymentIntent {}", intent.getId());
            return;
        }
        if (payment.getStatus() == PaymentStatusEnum.SUCCEEDED) {
            return;
        }

        payment.setStatus(PaymentStatusEnum.SUCCEEDED);
        payment.setPaidAt(LocalDateTime.now());
        payment.setStripeChargeId(intent.getLatestCharge());
        payment = paymentDao.save(payment);

        notificationService.create(payment.getFamilyUserId(), NotificationTypeEnum.PAYMENT_RECEIVED,
                "Payment received", "Your payment of " + payment.getAmount() + " " + payment.getCurrency().toUpperCase()
                        + " was received.", null);

        Booking booking = bookingDao.findById(payment.getBookingId(), true);
        if (booking != null) {
            CaretakerProfile caretakerProfile = caretakerProfileDao.findById(booking.getCaretakerId(), true);
            if (caretakerProfile != null) {
                notificationService.create(caretakerProfile.getUserId(), NotificationTypeEnum.PAYMENT_RECEIVED,
                        "Payment received", "A payment of " + payment.getAmount() + " " + payment.getCurrency().toUpperCase()
                                + " was received for booking #" + booking.getId() + ".", null);
            }
        }
    }

    private void handleFailed(PaymentIntent intent) {
        Payment payment = paymentDao.findByStripePaymentIntentId(intent.getId()).orElse(null);
        if (payment == null) {
            log.warn("Stripe payment_intent.payment_failed for unknown PaymentIntent {}", intent.getId());
            return;
        }
        if (payment.getStatus() == PaymentStatusEnum.SUCCEEDED || payment.getStatus() == PaymentStatusEnum.REFUNDED) {
            return;
        }

        String reason = intent.getLastPaymentError() != null ? intent.getLastPaymentError().getMessage() : "Payment failed";
        payment.setStatus(PaymentStatusEnum.FAILED);
        payment.setFailureReason(reason);
        payment = paymentDao.save(payment);

        notificationService.create(payment.getFamilyUserId(), NotificationTypeEnum.PAYMENT_FAILED,
                "Payment failed", "Your payment attempt failed: " + reason, null);
    }

    @Override
    public PaymentResponse getById(Long id, Long callerUserId) throws GenericException {
        Payment payment = paymentDao.findById(id, true);
        if (payment == null) {
            throw new GenericException(ExceptionCodeEnum.PAYMENT_NOT_FOUND, "Payment not found");
        }
        assertAccess(payment, callerUserId);
        return toResponse(payment);
    }

    @Override
    public PaymentResponse getByBooking(Long bookingId, Long callerUserId) throws GenericException {
        List<Payment> payments = paymentDao.findByBookingId(bookingId);
        if (payments.isEmpty()) {
            throw new GenericException(ExceptionCodeEnum.PAYMENT_NOT_FOUND, "No payment found for this booking");
        }
        Payment payment = payments.get(0);
        assertAccess(payment, callerUserId);
        return toResponse(payment);
    }

    private void assertAccess(Payment payment, Long callerUserId) throws GenericException {
        if (payment.getFamilyUserId().equals(callerUserId)) {
            return;
        }
        Booking booking = bookingDao.findById(payment.getBookingId(), true);
        if (booking != null) {
            CaretakerProfile caretakerProfile = caretakerProfileDao.findById(booking.getCaretakerId(), true);
            if (caretakerProfile != null && caretakerProfile.getUserId().equals(callerUserId)) {
                return;
            }
        }
        User caller = userDao.findById(callerUserId, true);
        if (caller != null && ADMIN_TIER.contains(caller.getUserType())) {
            return;
        }
        throw new GenericException(ExceptionCodeEnum.FORBIDDEN, "You do not have access to this payment");
    }

    @Override
    public Page<PaymentResponse> search(Long bookingId, Long familyUserId, PaymentStatusEnum status, Pageable pageable) {
        return paymentDao.search(bookingId, familyUserId, status, pageable).map(this::toResponse);
    }

    @Override
    public EarningsResponse getMyEarnings(Long callerUserId) throws GenericException {
        CaretakerProfile profile = caretakerProfileDao.findByUserId(callerUserId)
                .orElseThrow(() -> new GenericException(ExceptionCodeEnum.CARETAKER_PROFILE_NOT_FOUND, "Caretaker profile not found"));
        return buildEarnings(profile.getId());
    }

    @Override
    public EarningsResponse getCaretakerEarnings(Long caretakerId) throws GenericException {
        CaretakerProfile profile = caretakerProfileDao.findById(caretakerId, true);
        if (profile == null) {
            throw new GenericException(ExceptionCodeEnum.CARETAKER_PROFILE_NOT_FOUND, "Caretaker not found");
        }
        return buildEarnings(caretakerId);
    }

    private EarningsResponse buildEarnings(Long caretakerId) {
        BigDecimal total = paymentDao.sumSucceededByCaretakerId(caretakerId);
        long count = paymentDao.countSucceededByCaretakerId(caretakerId);
        List<EarningsResponse.MonthlyEarning> breakdown = paymentDao.monthlyEarningsRaw(caretakerId).stream()
                .map(row -> EarningsResponse.MonthlyEarning.builder()
                        .month(((java.sql.Date) row[0]).toLocalDate())
                        .total((BigDecimal) row[1])
                        .build())
                .toList();

        return EarningsResponse.builder()
                .caretakerId(caretakerId)
                .totalEarned(total)
                .succeededPaymentCount(count)
                .monthlyBreakdown(breakdown)
                .build();
    }

    @Override
    @Transactional
    public PaymentResponse refund(Long paymentId, Long adminUserId) throws GenericException {
        Payment payment = paymentDao.findById(paymentId, true);
        if (payment == null) {
            throw new GenericException(ExceptionCodeEnum.PAYMENT_NOT_FOUND, "Payment not found");
        }
        if (payment.getStatus() != PaymentStatusEnum.SUCCEEDED) {
            throw new GenericException(ExceptionCodeEnum.PAYMENT_NOT_REFUNDABLE, "Only a SUCCEEDED payment can be refunded");
        }
        if (!stripeConfig.isConfigured()) {
            throw new GenericException(ExceptionCodeEnum.PAYMENT_GATEWAY_NOT_CONFIGURED, "Payments are not configured on this server");
        }

        try {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(payment.getStripePaymentIntentId())
                    .build();
            Refund.create(params);
        } catch (StripeException e) {
            throw new IllegalStateException("Failed to refund Stripe PaymentIntent " + payment.getStripePaymentIntentId(), e);
        }

        payment.setStatus(PaymentStatusEnum.REFUNDED);
        payment.setRefundedAt(LocalDateTime.now());
        payment = paymentDao.save(payment);

        notificationService.create(payment.getFamilyUserId(), NotificationTypeEnum.GENERAL,
                "Payment refunded", "Your payment of " + payment.getAmount() + " " + payment.getCurrency().toUpperCase()
                        + " has been refunded.", null);

        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setBookingId(payment.getBookingId());
        response.setFamilyUserId(payment.getFamilyUserId());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setStatus(payment.getStatus());
        response.setStripePaymentIntentId(payment.getStripePaymentIntentId());
        response.setStripeChargeId(payment.getStripeChargeId());
        response.setFailureReason(payment.getFailureReason());
        response.setPaidAt(payment.getPaidAt());
        response.setRefundedAt(payment.getRefundedAt());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        return response;
    }
}
