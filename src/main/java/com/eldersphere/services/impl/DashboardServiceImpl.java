package com.eldersphere.services.impl;

import com.eldersphere.dao.booking.BookingDao;
import com.eldersphere.dao.caretaker.CaretakerProfileDao;
import com.eldersphere.dao.elder.ElderProfileDao;
import com.eldersphere.dao.emergency.EmergencyAlertDao;
import com.eldersphere.dao.medicalrecord.MedicalRecordDao;
import com.eldersphere.dao.notification.NotificationDao;
import com.eldersphere.dao.user.UserDao;
import com.eldersphere.dtos.Dashboard.ActivityDTO;
import com.eldersphere.dtos.Dashboard.CaretakerDashboardSummaryDTO;
import com.eldersphere.dtos.Dashboard.DashboardSummaryDTO;
import com.eldersphere.dtos.Dashboard.ElderDashboardSummaryDTO;
import com.eldersphere.dtos.Dashboard.FamilyDashboardSummaryDTO;
import com.eldersphere.entities.Booking;
import com.eldersphere.entities.CaretakerProfile;
import com.eldersphere.entities.ElderProfile;
import com.eldersphere.entities.EmergencyAlert;
import com.eldersphere.entities.MedicalRecord;
import com.eldersphere.enums.BookingStatusEnum;
import com.eldersphere.enums.CaretakerVerificationStatusEnum;
import com.eldersphere.enums.EmergencyAlertStatusEnum;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.enums.UserTypeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final List<BookingStatusEnum> ACTIVE_BOOKING_STATUSES =
            List.of(BookingStatusEnum.PENDING, BookingStatusEnum.CONFIRMED, BookingStatusEnum.IN_PROGRESS);

    private static final List<EmergencyAlertStatusEnum> ACTIVE_ALERT_STATUSES =
            List.of(EmergencyAlertStatusEnum.TRIGGERED, EmergencyAlertStatusEnum.ACKNOWLEDGED);

    private final BookingDao bookingDao;
    private final CaretakerProfileDao caretakerProfileDao;
    private final EmergencyAlertDao emergencyAlertDao;
    private final ElderProfileDao elderProfileDao;
    private final MedicalRecordDao medicalRecordDao;
    private final NotificationDao notificationDao;
    private final UserDao userDao;

    @Override
    public DashboardSummaryDTO getSummary() {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last30Days = now.minusDays(30);

        long totalBookings = bookingDao.getRepository().count();
        long activeCaretakers = caretakerProfileDao.countByVerificationStatus(CaretakerVerificationStatusEnum.VERIFIED);
        long pendingEmergencyAlerts = emergencyAlertDao.countByStatus(EmergencyAlertStatusEnum.TRIGGERED)
                + emergencyAlertDao.countByStatus(EmergencyAlertStatusEnum.ACKNOWLEDGED);
        long totalElders = userDao.countByUserType(UserTypeEnum.ELDER);
        long totalFamilies = userDao.countByUserType(UserTypeEnum.FAMILY_MEMBER);

        BigDecimal revenueThisMonth = bookingDao.sumRevenueBetween(startOfMonth, now);
        BigDecimal revenueLast30Days = bookingDao.sumRevenueBetween(last30Days, now);

        Stream<ActivityDTO> bookingActivity = bookingDao.findRecent().stream()
                .map(this::toBookingActivity);
        Stream<ActivityDTO> alertActivity = emergencyAlertDao.findRecent().stream()
                .map(this::toAlertActivity);

        List<ActivityDTO> recentActivities = Stream.concat(bookingActivity, alertActivity)
                .sorted(this::byTimestampDesc)
                .limit(10)
                .collect(Collectors.toList());

        return DashboardSummaryDTO.builder()
                .totalBookings(totalBookings)
                .activeCaretakers(activeCaretakers)
                .pendingEmergencyAlerts(pendingEmergencyAlerts)
                .totalElders(totalElders)
                .totalFamilies(totalFamilies)
                .revenueThisMonth(revenueThisMonth != null ? revenueThisMonth : BigDecimal.ZERO)
                .revenueLast30Days(revenueLast30Days != null ? revenueLast30Days : BigDecimal.ZERO)
                .recentActivities(recentActivities)
                .build();
    }

    @Override
    public FamilyDashboardSummaryDTO getFamilySummary(Long familyUserId) {
        List<ElderProfile> managedElders = elderProfileDao.findByFamilyUserId(familyUserId);

        long upcomingBookings = bookingDao.countByFamilyUserIdAndStatusIn(familyUserId, ACTIVE_BOOKING_STATUSES);
        long unreadNotifications = notificationDao.countUnread(familyUserId);

        Stream<ActivityDTO> bookingActivity = bookingDao.findRecentByFamilyUserId(familyUserId).stream()
                .map(this::toBookingActivity);
        Stream<ActivityDTO> medicalRecordActivity = managedElders.stream()
                .flatMap(elder -> medicalRecordDao.findSharedByElderProfileId(elder.getId(), PageRequest.of(0, 5)).stream())
                .map(this::toMedicalRecordActivity);

        List<ActivityDTO> recentActivities = Stream.concat(bookingActivity, medicalRecordActivity)
                .sorted(this::byTimestampDesc)
                .limit(10)
                .collect(Collectors.toList());

        return FamilyDashboardSummaryDTO.builder()
                .managedElderCount(managedElders.size())
                .upcomingBookings(upcomingBookings)
                .unreadNotifications(unreadNotifications)
                .recentActivities(recentActivities)
                .build();
    }

    @Override
    public CaretakerDashboardSummaryDTO getCaretakerSummary(Long caretakerUserId) throws GenericException {
        CaretakerProfile profile = caretakerProfileDao.findByUserId(caretakerUserId)
                .orElseThrow(() -> new GenericException(ExceptionCodeEnum.CARETAKER_PROFILE_NOT_FOUND, "Caretaker profile not found"));

        long upcomingBookings = bookingDao.countByCaretakerIdAndStatusIn(profile.getId(), ACTIVE_BOOKING_STATUSES);
        long completedBookings = bookingDao.countByCaretakerIdAndStatus(profile.getId(), BookingStatusEnum.COMPLETED);
        long unreadNotifications = notificationDao.countUnread(caretakerUserId);

        List<ActivityDTO> recentActivities = bookingDao.findRecentByCaretakerId(profile.getId()).stream()
                .map(this::toBookingActivity)
                .sorted(this::byTimestampDesc)
                .limit(10)
                .collect(Collectors.toList());

        return CaretakerDashboardSummaryDTO.builder()
                .upcomingBookings(upcomingBookings)
                .completedBookings(completedBookings)
                .averageRating(profile.getRatingAverage())
                .unreadNotifications(unreadNotifications)
                .recentActivities(recentActivities)
                .build();
    }

    @Override
    public ElderDashboardSummaryDTO getElderSummary(Long elderUserId) throws GenericException {
        ElderProfile profile = elderProfileDao.findByElderUserId(elderUserId)
                .orElseThrow(() -> new GenericException(ExceptionCodeEnum.ELDER_PROFILE_NOT_FOUND, "Elder profile not found"));

        long upcomingBookings = bookingDao.countByElderProfileIdAndStatusIn(profile.getId(), ACTIVE_BOOKING_STATUSES);
        long activeEmergencyAlerts = emergencyAlertDao.countByElderProfileIdAndStatusIn(profile.getId(), ACTIVE_ALERT_STATUSES);
        long unreadNotifications = notificationDao.countUnread(elderUserId);

        List<ActivityDTO> recentMedicalRecords = medicalRecordDao.findSharedByElderProfileId(profile.getId(), PageRequest.of(0, 10))
                .stream()
                .map(this::toMedicalRecordActivity)
                .sorted(this::byTimestampDesc)
                .collect(Collectors.toList());

        return ElderDashboardSummaryDTO.builder()
                .upcomingBookings(upcomingBookings)
                .activeEmergencyAlerts(activeEmergencyAlerts)
                .unreadNotifications(unreadNotifications)
                .recentMedicalRecords(recentMedicalRecords)
                .build();
    }

    private int byTimestampDesc(ActivityDTO a, ActivityDTO b) {
        if (a.getTimestamp() == null || b.getTimestamp() == null) return 0;
        return b.getTimestamp().compareTo(a.getTimestamp());
    }

    private ActivityDTO toBookingActivity(Booking booking) {
        return ActivityDTO.builder()
                .type("BOOKING")
                .description("Booking #" + booking.getId() + " is " + booking.getStatus())
                .timestamp(booking.getCreatedAt())
                .entityId(String.valueOf(booking.getId()))
                .build();
    }

    private ActivityDTO toAlertActivity(EmergencyAlert alert) {
        return ActivityDTO.builder()
                .type("EMERGENCY_ALERT")
                .description("Emergency alert #" + alert.getId() + " is " + alert.getStatus())
                .timestamp(alert.getTriggeredAt())
                .entityId(String.valueOf(alert.getId()))
                .build();
    }

    private ActivityDTO toMedicalRecordActivity(MedicalRecord record) {
        return ActivityDTO.builder()
                .type("MEDICAL_RECORD")
                .description("Medical record \"" + record.getTitle() + "\" (" + record.getType() + ") was updated")
                .timestamp(record.getCreatedAt())
                .entityId(String.valueOf(record.getId()))
                .build();
    }
}
