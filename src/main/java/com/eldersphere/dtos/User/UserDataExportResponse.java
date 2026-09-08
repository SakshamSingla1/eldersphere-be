package com.eldersphere.dtos.User;

import com.eldersphere.dtos.Booking.BookingResponse;
import com.eldersphere.dtos.MedicalRecord.MedicalRecordResponse;
import com.eldersphere.dtos.Notification.NotificationResponseDTO;
import com.eldersphere.dtos.Review.ReviewResponse;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * "Download my data" export: a simple DTO aggregating existing per-module service calls for
 * the caller's own data - not a new reporting engine, no new persistence.
 */
@Data
@Builder
public class UserDataExportResponse {
    private LocalDateTime exportedAt;
    private UserResponse profile;
    private List<BookingResponse> bookings;
    private List<ReviewResponse> reviews;
    private List<MedicalRecordResponse> medicalRecords;
    private List<NotificationResponseDTO> notifications;
}
