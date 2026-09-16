package com.eldersphere.controllers;

import com.eldersphere.dtos.Payment.CreatePaymentIntentRequest;
import com.eldersphere.dtos.Payment.CreatePaymentIntentResponse;
import com.eldersphere.dtos.Payment.EarningsResponse;
import com.eldersphere.dtos.Payment.PaymentResponse;
import com.eldersphere.enums.PaymentStatusEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import com.eldersphere.services.PaymentService;
import com.eldersphere.utils.Helper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Stripe-backed booking payments and caretaker earnings")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final Helper helper;

    @Operation(summary = "Create or reuse a payment intent", description = "Called by the booking's family member. Reuses an existing PENDING Stripe PaymentIntent for this booking instead of creating a duplicate.")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/intent")
    public ResponseEntity<ResponseModel<CreatePaymentIntentResponse>> createIntent(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @Valid @RequestBody CreatePaymentIntentRequest request) throws GenericException {
        Long userId = helper.getUserIdFromHeader(auth);
        return ApiResponse.createSuccess(paymentService.createIntent(request.getBookingId(), userId), "Payment intent created successfully");
    }

    @Operation(summary = "Stripe webhook", description = "Called by Stripe only. The raw request body and Stripe-Signature header are verified with the Stripe SDK before any event is applied - see application.properties' stripe.webhook-secret and SecurityConfig, which permits this path without a JWT.")
    @PostMapping("/webhook")
    public ResponseEntity<ResponseModel<Void>> webhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) throws GenericException {
        paymentService.handleWebhook(payload, signature);
        return ApiResponse.successResponse();
    }

    @Operation(summary = "Get payment by ID", description = "Restricted to the payment's own family member, the booking's caretaker, or an admin.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseModel<PaymentResponse>> getById(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long id) throws GenericException {
        Long userId = helper.getUserIdFromHeader(auth);
        return ApiResponse.successResponse(paymentService.getById(id, userId), "Payment fetched successfully");
    }

    @Operation(summary = "Get the most recent payment for a booking", description = "Restricted to the booking's own family member, its caretaker, or an admin.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ResponseModel<PaymentResponse>> getByBooking(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long bookingId) throws GenericException {
        Long userId = helper.getUserIdFromHeader(auth);
        return ApiResponse.successResponse(paymentService.getByBooking(bookingId, userId), "Payment fetched successfully");
    }

    @Operation(summary = "Search payments (admin)", description = "Paginated payment search, filterable by booking, family user, and status.")
    @PreAuthorize("isAuthenticated() and @adminPermissionGuard.has('PAYMENTS_VIEW')")
    @GetMapping
    public ResponseEntity<ResponseModel<Page<PaymentResponse>>> search(
            @RequestParam(required = false) Long bookingId,
            @RequestParam(required = false) Long familyUserId,
            @RequestParam(required = false) PaymentStatusEnum status,
            Pageable pageable) {
        return ApiResponse.successResponse(paymentService.search(bookingId, familyUserId, status, pageable), "Payments fetched successfully");
    }

    @Operation(summary = "Get my earnings", description = "Called by a logged-in CARETAKER user; resolves their caretaker profile from the JWT the same way PUT /caretakers/me/availability does.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me/earnings")
    public ResponseEntity<ResponseModel<EarningsResponse>> getMyEarnings(
            @RequestHeader(value = "Authorization", required = false) String auth) throws GenericException {
        Long userId = helper.getUserIdFromHeader(auth);
        return ApiResponse.successResponse(paymentService.getMyEarnings(userId), "Earnings fetched successfully");
    }

    @Operation(summary = "Get a caretaker's earnings (admin)")
    @PreAuthorize("isAuthenticated() and @adminPermissionGuard.has('PAYMENTS_VIEW')")
    @GetMapping("/caretaker/{id}/earnings")
    public ResponseEntity<ResponseModel<EarningsResponse>> getCaretakerEarnings(@PathVariable Long id) throws GenericException {
        return ApiResponse.successResponse(paymentService.getCaretakerEarnings(id), "Earnings fetched successfully");
    }

    @Operation(summary = "Refund a payment (admin)")
    @PreAuthorize("isAuthenticated() and @adminPermissionGuard.has('PAYMENTS_MANAGE')")
    @PutMapping("/{id}/refund")
    public ResponseEntity<ResponseModel<PaymentResponse>> refund(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long id) throws GenericException {
        Long adminUserId = helper.getUserIdFromHeader(auth);
        return ApiResponse.successResponse(paymentService.refund(id, adminUserId), "Payment refunded successfully");
    }
}
