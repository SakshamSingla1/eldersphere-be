package com.eldersphere.services.impl;

import com.eldersphere.dao.booking.BookingDao;
import com.eldersphere.dao.caretaker.CaretakerAvailabilityDao;
import com.eldersphere.dao.caretaker.CaretakerProfileDao;
import com.eldersphere.dao.elder.ElderProfileDao;
import com.eldersphere.dao.service.ServiceOfferingDao;
import com.eldersphere.dao.user.UserDao;
import com.eldersphere.dtos.Booking.BookingRequest;
import com.eldersphere.dtos.Booking.BookingResponse;
import com.eldersphere.entities.Booking;
import com.eldersphere.entities.CaretakerAvailability;
import com.eldersphere.entities.CaretakerProfile;
import com.eldersphere.entities.ElderProfile;
import com.eldersphere.entities.ServiceOffering;
import com.eldersphere.entities.User;
import com.eldersphere.enums.BookingStatusEnum;
import com.eldersphere.enums.DayOfWeekEnum;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.enums.NotificationTypeEnum;
import com.eldersphere.enums.UserTypeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.services.BookingService;
import com.eldersphere.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final List<BookingStatusEnum> ACTIVE_STATUSES =
            List.of(BookingStatusEnum.PENDING, BookingStatusEnum.CONFIRMED, BookingStatusEnum.IN_PROGRESS);

    private static final Set<UserTypeEnum> ADMIN_TIER = EnumSet.of(UserTypeEnum.ADMIN, UserTypeEnum.SUPER_ADMIN);

    private static final int DEFAULT_DURATION_MINUTES = 60;

    private final BookingDao bookingDao;
    private final ElderProfileDao elderProfileDao;
    private final CaretakerProfileDao caretakerProfileDao;
    private final ServiceOfferingDao serviceOfferingDao;
    private final CaretakerAvailabilityDao caretakerAvailabilityDao;
    private final UserDao userDao;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public BookingResponse create(BookingRequest request) throws GenericException {
        ElderProfile elderProfile = elderProfileDao.findById(request.getElderProfileId(), true);
        if (elderProfile == null) {
            throw new GenericException(ExceptionCodeEnum.ELDER_PROFILE_NOT_FOUND, "Elder profile not found");
        }
        CaretakerProfile caretakerProfile = caretakerProfileDao.findById(request.getCaretakerId(), true);
        if (caretakerProfile == null) {
            throw new GenericException(ExceptionCodeEnum.CARETAKER_PROFILE_NOT_FOUND, "Caretaker not found");
        }
        ServiceOffering service = serviceOfferingDao.findById(request.getServiceId(), true);
        if (service == null) {
            throw new GenericException(ExceptionCodeEnum.SERVICE_OFFERING_NOT_FOUND, "Service offering not found");
        }

        int durationMinutes = service.getDurationMinutes() != null ? service.getDurationMinutes() : DEFAULT_DURATION_MINUTES;
        Long familyUserId = request.getFamilyUserId() != null ? request.getFamilyUserId() : elderProfile.getFamilyUserId();

        boolean recurring = Boolean.TRUE.equals(request.getRepeatWeekly());
        if (recurring) {
            Integer occurrences = request.getOccurrences();
            if (occurrences == null || occurrences < 2 || occurrences > 12) {
                throw new GenericException(ExceptionCodeEnum.VALIDATION_FAILED,
                        "occurrences must be between 2 and 12 when repeatWeekly is true");
            }

            // Policy: validate every occurrence up front and fail the whole series
            // (creating nothing) if any single occurrence conflicts. This keeps the
            // outcome all-or-nothing and easy to reason about, rather than leaving
            // a partially-booked series for the caller to reconcile.
            String recurringGroupId = UUID.randomUUID().toString();
            List<Booking> occurrenceBookings = new ArrayList<>();
            for (int i = 0; i < occurrences; i++) {
                LocalDate occurrenceDate = request.getScheduledDate().plusWeeks(i);
                validateSlot(request.getCaretakerId(), occurrenceDate, request.getScheduledTime(), durationMinutes, i + 1);

                occurrenceBookings.add(Booking.builder()
                        .familyUserId(familyUserId)
                        .elderProfileId(request.getElderProfileId())
                        .caretakerId(request.getCaretakerId())
                        .serviceId(request.getServiceId())
                        .scheduledDate(occurrenceDate)
                        .scheduledTime(request.getScheduledTime())
                        .status(BookingStatusEnum.PENDING)
                        .cost(service.getBasePrice())
                        .notes(request.getNotes())
                        .recurringGroupId(recurringGroupId)
                        .build());
            }

            List<Booking> saved = bookingDao.saveAll(occurrenceBookings);

            notificationService.create(caretakerProfile.getUserId(), NotificationTypeEnum.BOOKING_CONFIRMED,
                    "New recurring booking request",
                    "You have " + occurrences + " new weekly booking requests for " + service.getName()
                            + " starting " + request.getScheduledDate(),
                    null);

            return toResponse(saved.get(0));
        }

        validateSlot(request.getCaretakerId(), request.getScheduledDate(), request.getScheduledTime(), durationMinutes, null);

        Booking booking = Booking.builder()
                .familyUserId(familyUserId)
                .elderProfileId(request.getElderProfileId())
                .caretakerId(request.getCaretakerId())
                .serviceId(request.getServiceId())
                .scheduledDate(request.getScheduledDate())
                .scheduledTime(request.getScheduledTime())
                .status(BookingStatusEnum.PENDING)
                .cost(service.getBasePrice())
                .notes(request.getNotes())
                .build();
        booking = bookingDao.save(booking);

        notificationService.create(caretakerProfile.getUserId(), NotificationTypeEnum.BOOKING_CONFIRMED,
                "New booking request",
                "You have a new booking request for " + service.getName() + " on " + request.getScheduledDate(),
                null);

        return toResponse(booking);
    }

    /**
     * Validates a single caretaker/date/time slot against:
     * 1) the caretaker's declared weekly availability windows, and
     * 2) any overlapping PENDING/CONFIRMED/IN_PROGRESS booking they already have.
     * {@code occurrenceNumber} is included in error messages for recurring series (null for a one-off booking).
     */
    private void validateSlot(Long caretakerId, LocalDate date, LocalTime startTime, int durationMinutes, Integer occurrenceNumber) throws GenericException {
        LocalTime endTime = startTime.plusMinutes(durationMinutes);
        String occurrenceLabel = occurrenceNumber != null ? " (occurrence " + occurrenceNumber + ", " + date + ")" : "";

        DayOfWeekEnum dayOfWeek = DayOfWeekEnum.valueOf(date.getDayOfWeek().name());
        List<CaretakerAvailability> slots = caretakerAvailabilityDao.findByCaretakerId(caretakerId);
        // A caretaker who hasn't declared any weekly availability yet (e.g. right after
        // verification) isn't restricted to zero bookable hours - treat "nothing declared" as
        // "always available" rather than rejecting every slot forever.
        boolean withinAvailability = slots.isEmpty() || slots.stream().anyMatch(slot ->
                slot.getDayOfWeek() == dayOfWeek
                        && !startTime.isBefore(slot.getStartTime())
                        && !endTime.isAfter(slot.getEndTime()));
        if (!withinAvailability) {
            throw new GenericException(ExceptionCodeEnum.CARETAKER_UNAVAILABLE,
                    "This caretaker is not available on " + dayOfWeek + " between " + startTime + " and " + endTime
                            + " - it falls outside their declared availability" + occurrenceLabel);
        }

        List<Booking> existingBookings = bookingDao.findByCaretakerIdAndDateAndStatuses(caretakerId, date, ACTIVE_STATUSES);
        for (Booking existing : existingBookings) {
            ServiceOffering existingService = serviceOfferingDao.findById(existing.getServiceId(), true);
            int existingDuration = (existingService != null && existingService.getDurationMinutes() != null)
                    ? existingService.getDurationMinutes() : DEFAULT_DURATION_MINUTES;
            LocalTime existingEnd = existing.getScheduledTime().plusMinutes(existingDuration);

            boolean overlaps = startTime.isBefore(existingEnd) && existing.getScheduledTime().isBefore(endTime);
            if (overlaps) {
                throw new GenericException(ExceptionCodeEnum.BOOKING_CONFLICT,
                        "This caretaker already has a booking overlapping " + startTime + "-" + endTime
                                + " on " + date + occurrenceLabel);
            }
        }
    }

    @Override
    @Transactional
    public BookingResponse updateStatus(Long id, BookingStatusEnum status) throws GenericException {
        Booking booking = bookingDao.findById(id, true);
        if (booking == null) {
            throw new GenericException(ExceptionCodeEnum.BOOKING_NOT_FOUND, "Booking not found");
        }
        booking.setStatus(status);
        booking = bookingDao.save(booking);

        if (status == BookingStatusEnum.CONFIRMED) {
            notificationService.create(booking.getFamilyUserId(), NotificationTypeEnum.BOOKING_CONFIRMED,
                    "Booking confirmed", "Your booking has been confirmed.", null);
        }

        return toResponse(booking);
    }

    @Override
    public BookingResponse getById(Long id) throws GenericException {
        Booking booking = bookingDao.findById(id, true);
        if (booking == null) {
            throw new GenericException(ExceptionCodeEnum.BOOKING_NOT_FOUND, "Booking not found");
        }
        return toResponse(booking);
    }

    @Override
    public Page<BookingResponse> search(Long familyUserId, Long caretakerId, BookingStatusEnum status, Pageable pageable) {
        return bookingDao.findByCriteria(familyUserId, caretakerId, status, pageable).map(this::toResponse);
    }

    @Override
    public List<BookingResponse> getByRecurringGroupId(String recurringGroupId) throws GenericException {
        List<Booking> bookings = bookingDao.findByRecurringGroupId(recurringGroupId);
        if (bookings.isEmpty()) {
            throw new GenericException(ExceptionCodeEnum.BOOKING_NOT_FOUND, "No recurring booking series found for this ID");
        }
        return bookings.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public List<BookingResponse> cancelSeries(String recurringGroupId, Long requestingUserId) throws GenericException {
        List<Booking> bookings = bookingDao.findByRecurringGroupId(recurringGroupId);
        if (bookings.isEmpty()) {
            throw new GenericException(ExceptionCodeEnum.BOOKING_NOT_FOUND, "No recurring booking series found for this ID");
        }

        User requester = userDao.findById(requestingUserId, true);
        if (requester == null) {
            throw new GenericException(ExceptionCodeEnum.USER_NOT_FOUND, "User not found");
        }
        boolean isAdmin = ADMIN_TIER.contains(requester.getUserType());
        boolean isOwner = bookings.stream().anyMatch(b -> Objects.equals(b.getFamilyUserId(), requestingUserId));
        if (!isAdmin && !isOwner) {
            throw new GenericException(ExceptionCodeEnum.FORBIDDEN, "You do not have access to cancel this recurring booking series");
        }

        bookings.forEach(b -> b.setStatus(BookingStatusEnum.CANCELLED));
        List<Booking> saved = bookingDao.saveAll(bookings);
        return saved.stream().map(this::toResponse).toList();
    }

    private BookingResponse toResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setFamilyUserId(booking.getFamilyUserId());
        response.setElderProfileId(booking.getElderProfileId());
        ElderProfile elder = elderProfileDao.findById(booking.getElderProfileId(), true);
        if (elder != null) response.setElderName(elder.getName());
        response.setCaretakerId(booking.getCaretakerId());
        CaretakerProfile caretaker = caretakerProfileDao.findById(booking.getCaretakerId(), true);
        if (caretaker != null) {
            User caretakerUser = userDao.findById(caretaker.getUserId(), true);
            if (caretakerUser != null) response.setCaretakerName(caretakerUser.getFullName());
        }
        response.setServiceId(booking.getServiceId());
        ServiceOffering service = serviceOfferingDao.findById(booking.getServiceId(), true);
        if (service != null) response.setServiceName(service.getName());
        response.setScheduledDate(booking.getScheduledDate());
        response.setScheduledTime(booking.getScheduledTime());
        response.setStatus(booking.getStatus());
        response.setCost(booking.getCost());
        response.setNotes(booking.getNotes());
        response.setRecurringGroupId(booking.getRecurringGroupId());
        response.setCreatedAt(booking.getCreatedAt());
        response.setUpdatedAt(booking.getUpdatedAt());
        return response;
    }
}
