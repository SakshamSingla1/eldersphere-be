package com.eldersphere.services.impl;

import com.eldersphere.dtos.Booking.BookingResponse;
import com.eldersphere.dtos.Elder.ElderProfileResponse;
import com.eldersphere.dtos.MedicalRecord.MedicalRecordResponse;
import com.eldersphere.dtos.User.UserDataExportResponse;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.services.AdminService;
import com.eldersphere.services.BookingService;
import com.eldersphere.services.ElderProfileService;
import com.eldersphere.services.MedicalRecordService;
import com.eldersphere.services.NotificationService;
import com.eldersphere.services.ReviewService;
import com.eldersphere.services.UserDataExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic "download my data" export - a DTO aggregating existing per-module service calls for
 * the caller's own data. Deliberately simple: no new persistence, no cross-module reporting
 * logic beyond what each module's own service already exposes.
 */
@Service
@RequiredArgsConstructor
public class UserDataExportServiceImpl implements UserDataExportService {

    private static final Pageable UNPAGED = Pageable.unpaged();

    private final AdminService adminService;
    private final BookingService bookingService;
    private final ReviewService reviewService;
    private final MedicalRecordService medicalRecordService;
    private final NotificationService notificationService;
    private final ElderProfileService elderProfileService;

    @Override
    public UserDataExportResponse export(Long userId) throws GenericException {
        return UserDataExportResponse.builder()
                .exportedAt(LocalDateTime.now())
                .profile(adminService.getUser(userId))
                .bookings(collectBookings(userId))
                .reviews(reviewService.getByReviewer(userId, UNPAGED).getContent())
                .medicalRecords(collectMedicalRecords(userId))
                .notifications(notificationService.getByUser(userId, UNPAGED).getContent())
                .build();
    }

    /** Merges bookings where this user is the family member with ones where they're the caretaker (multi-role users can be both). */
    private List<BookingResponse> collectBookings(Long userId) {
        Map<Long, BookingResponse> byId = new LinkedHashMap<>();
        for (BookingResponse booking : bookingService.search(userId, null, null, UNPAGED).getContent()) {
            byId.put(booking.getId(), booking);
        }
        for (BookingResponse booking : bookingService.search(null, userId, null, UNPAGED).getContent()) {
            byId.put(booking.getId(), booking);
        }
        return new ArrayList<>(byId.values());
    }

    /** Medical records for every elder profile this user owns or manages - family-managed and/or self-managed. */
    private List<MedicalRecordResponse> collectMedicalRecords(Long userId) {
        Map<Long, Long> elderProfileIds = new LinkedHashMap<>();
        for (ElderProfileResponse profile : elderProfileService.getByFamilyUserId(userId)) {
            elderProfileIds.put(profile.getId(), profile.getId());
        }
        try {
            ElderProfileResponse own = elderProfileService.getByElderUserId(userId);
            if (own != null) {
                elderProfileIds.put(own.getId(), own.getId());
            }
        } catch (GenericException e) {
            // Not a self-managed elder - nothing to add.
        }

        Map<Long, MedicalRecordResponse> byId = new LinkedHashMap<>();
        for (Long elderProfileId : elderProfileIds.values()) {
            for (MedicalRecordResponse record : medicalRecordService.getByElderProfile(elderProfileId, false, UNPAGED).getContent()) {
                byId.put(record.getId(), record);
            }
        }
        return new ArrayList<>(byId.values());
    }
}
