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
import com.eldersphere.enums.ServiceCategoryEnum;
import com.eldersphere.enums.UserTypeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.services.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the booking-conflict / availability validation and recurring-series
 * all-or-nothing policy in {@link BookingServiceImpl} - the highest-value business logic
 * in this module, with every collaborator mocked.
 */
class BookingServiceImplTest {

    private static final Long CARETAKER_ID = 1L;
    private static final Long ELDER_PROFILE_ID = 10L;
    private static final Long SERVICE_ID = 20L;
    private static final Long CARETAKER_USER_ID = 200L;
    private static final Long FAMILY_USER_ID = 300L;

    private BookingDao bookingDao;
    private ElderProfileDao elderProfileDao;
    private CaretakerProfileDao caretakerProfileDao;
    private ServiceOfferingDao serviceOfferingDao;
    private CaretakerAvailabilityDao caretakerAvailabilityDao;
    private UserDao userDao;
    private NotificationService notificationService;
    private BookingServiceImpl bookingService;

    private LocalDate monday;

    @BeforeEach
    void setUp() {
        bookingDao = mock(BookingDao.class);
        elderProfileDao = mock(ElderProfileDao.class);
        caretakerProfileDao = mock(CaretakerProfileDao.class);
        serviceOfferingDao = mock(ServiceOfferingDao.class);
        caretakerAvailabilityDao = mock(CaretakerAvailabilityDao.class);
        userDao = mock(UserDao.class);
        notificationService = mock(NotificationService.class);

        bookingService = new BookingServiceImpl(
                bookingDao, elderProfileDao, caretakerProfileDao, serviceOfferingDao,
                caretakerAvailabilityDao, userDao, notificationService);

        // A deterministic Monday, far enough in the future to be a "normal" booking date.
        monday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));

        ElderProfile elder = ElderProfile.builder().id(ELDER_PROFILE_ID).familyUserId(FAMILY_USER_ID).name("Eleanor").build();
        when(elderProfileDao.findById(eq(ELDER_PROFILE_ID), eq(true))).thenReturn(elder);

        CaretakerProfile caretaker = CaretakerProfile.builder().id(CARETAKER_ID).userId(CARETAKER_USER_ID).build();
        when(caretakerProfileDao.findById(eq(CARETAKER_ID), eq(true))).thenReturn(caretaker);

        ServiceOffering service = ServiceOffering.builder()
                .id(SERVICE_ID).name("Nursing Care").category(ServiceCategoryEnum.NURSING)
                .basePrice(BigDecimal.valueOf(40)).durationMinutes(60).build();
        when(serviceOfferingDao.findById(eq(SERVICE_ID), eq(true))).thenReturn(service);

        User caretakerUser = User.builder().id(CARETAKER_USER_ID).fullName("Caretaker Demo").build();
        when(userDao.findById(eq(CARETAKER_USER_ID), eq(true))).thenReturn(caretakerUser);

        CaretakerAvailability mondaySlot = CaretakerAvailability.builder()
                .caretakerId(CARETAKER_ID).dayOfWeek(DayOfWeekEnum.MONDAY)
                .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(17, 0)).build();
        when(caretakerAvailabilityDao.findByCaretakerId(CARETAKER_ID)).thenReturn(List.of(mondaySlot));

        when(bookingDao.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            if (b.getId() == null) b.setId(999L);
            return b;
        });
    }

    private BookingRequest.BookingRequestBuilder baseRequest() {
        return BookingRequest.builder()
                .elderProfileId(ELDER_PROFILE_ID)
                .caretakerId(CARETAKER_ID)
                .serviceId(SERVICE_ID)
                .scheduledDate(monday)
                .scheduledTime(LocalTime.of(10, 0));
    }

    // ---------------------------------------------------------------
    // Single (non-recurring) booking
    // ---------------------------------------------------------------

    @Test
    void create_singleBooking_withinAvailabilityAndNoConflict_succeeds() throws GenericException {
        when(bookingDao.findByCaretakerIdAndDateAndStatuses(eq(CARETAKER_ID), eq(monday), anyList()))
                .thenReturn(List.of());

        BookingResponse response = bookingService.create(baseRequest().build());

        assertThat(response.getCaretakerId()).isEqualTo(CARETAKER_ID);
        assertThat(response.getStatus()).isEqualTo(BookingStatusEnum.PENDING);
        verify(notificationService).create(eq(CARETAKER_USER_ID), eq(NotificationTypeEnum.BOOKING_CONFIRMED), any(), any(), any());
    }

    @Test
    void create_outsideDeclaredAvailability_throwsCaretakerUnavailable() {
        BookingRequest request = baseRequest().scheduledTime(LocalTime.of(20, 0)).build();

        GenericException ex = assertThrows(GenericException.class, () -> bookingService.create(request));

        assertThat(ex).isNotNull();
        assertThat(ex.getExceptionCode()).isEqualTo(ExceptionCodeEnum.CARETAKER_UNAVAILABLE);
        verify(bookingDao, never()).save(any());
    }

    @Test
    void create_overlappingExistingBooking_throwsBookingConflict() {
        Booking existing = Booking.builder()
                .id(5L).caretakerId(CARETAKER_ID).serviceId(SERVICE_ID)
                .scheduledDate(monday).scheduledTime(LocalTime.of(10, 30))
                .status(BookingStatusEnum.CONFIRMED).build();
        when(bookingDao.findByCaretakerIdAndDateAndStatuses(eq(CARETAKER_ID), eq(monday), anyList()))
                .thenReturn(List.of(existing));
        when(serviceOfferingDao.findById(eq(SERVICE_ID), eq(true)))
                .thenReturn(ServiceOffering.builder().id(SERVICE_ID).durationMinutes(60).basePrice(BigDecimal.TEN).build());

        BookingRequest request = baseRequest().build(); // 10:00-11:00 overlaps existing 10:30-11:30

        GenericException ex = assertThrows(GenericException.class, () -> bookingService.create(request));

        assertThat(ex).isNotNull();
        assertThat(ex.getExceptionCode()).isEqualTo(ExceptionCodeEnum.BOOKING_CONFLICT);
        verify(bookingDao, never()).save(any());
    }

    @Test
    void create_nonOverlappingAdjacentBooking_succeeds() throws GenericException {
        // Existing booking ends exactly when the new one starts (09:00-10:00 vs 10:00-11:00) - not a conflict.
        Booking existing = Booking.builder()
                .id(5L).caretakerId(CARETAKER_ID).serviceId(SERVICE_ID)
                .scheduledDate(monday).scheduledTime(LocalTime.of(9, 0))
                .status(BookingStatusEnum.CONFIRMED).build();
        when(bookingDao.findByCaretakerIdAndDateAndStatuses(eq(CARETAKER_ID), eq(monday), anyList()))
                .thenReturn(List.of(existing));

        BookingResponse response = bookingService.create(baseRequest().build());

        assertThat(response).isNotNull();
    }

    @Test
    void create_elderProfileNotFound_throwsElderProfileNotFound() {
        when(elderProfileDao.findById(eq(ELDER_PROFILE_ID), eq(true))).thenReturn(null);

        GenericException ex = assertThrows(GenericException.class, () -> bookingService.create(baseRequest().build()));

        assertThat(ex).isNotNull();
        assertThat(ex.getExceptionCode()).isEqualTo(ExceptionCodeEnum.ELDER_PROFILE_NOT_FOUND);
    }

    // ---------------------------------------------------------------
    // Recurring series: validation range + all-or-nothing policy
    // ---------------------------------------------------------------

    @Test
    void create_recurring_occurrencesBelowMinimum_throwsValidationFailed() {
        BookingRequest request = baseRequest().repeatWeekly(true).occurrences(1).build();

        GenericException ex = assertThrows(GenericException.class, () -> bookingService.create(request));

        assertThat(ex).isNotNull();
        assertThat(ex.getExceptionCode()).isEqualTo(ExceptionCodeEnum.VALIDATION_FAILED);
        verify(bookingDao, never()).saveAll(any());
    }

    @Test
    void create_recurring_occurrencesAboveMaximum_throwsValidationFailed() {
        BookingRequest request = baseRequest().repeatWeekly(true).occurrences(13).build();

        GenericException ex = assertThrows(GenericException.class, () -> bookingService.create(request));

        assertThat(ex).isNotNull();
        assertThat(ex.getExceptionCode()).isEqualTo(ExceptionCodeEnum.VALIDATION_FAILED);
        verify(bookingDao, never()).saveAll(any());
    }

    @Test
    void create_recurring_allOccurrencesValid_savesWholeSeriesAtOnce() throws GenericException {
        for (int i = 0; i < 3; i++) {
            LocalDate occurrenceDate = monday.plusWeeks(i);
            when(bookingDao.findByCaretakerIdAndDateAndStatuses(eq(CARETAKER_ID), eq(occurrenceDate), anyList()))
                    .thenReturn(List.of());
        }
        when(bookingDao.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        BookingRequest request = baseRequest().repeatWeekly(true).occurrences(3).build();
        BookingResponse response = bookingService.create(request);

        assertThat(response).isNotNull();
        ArgumentCaptor<List<Booking>> captor = ArgumentCaptor.forClass(List.class);
        verify(bookingDao, times(1)).saveAll(captor.capture());
        List<Booking> saved = captor.getValue();
        assertThat(saved).hasSize(3);
        assertThat(saved).allSatisfy(b -> assertThat(b.getRecurringGroupId()).isNotBlank());
        // Every occurrence in the series shares the same recurring group id.
        assertThat(saved.stream().map(Booking::getRecurringGroupId).distinct()).hasSize(1);
    }

    @Test
    void create_recurring_oneOccurrenceConflicts_failsWholeSeriesAllOrNothing() {
        // Occurrences 1 and 2 (weeks 0 and 1) are conflict-free; occurrence 3 (week 2)
        // collides with an existing booking. The whole series must be rejected and
        // nothing persisted - bookingDao.saveAll must never be invoked.
        LocalDate week0 = monday;
        LocalDate week1 = monday.plusWeeks(1);
        LocalDate week2 = monday.plusWeeks(2);

        when(bookingDao.findByCaretakerIdAndDateAndStatuses(eq(CARETAKER_ID), eq(week0), anyList())).thenReturn(List.of());
        when(bookingDao.findByCaretakerIdAndDateAndStatuses(eq(CARETAKER_ID), eq(week1), anyList())).thenReturn(List.of());

        Booking conflicting = Booking.builder()
                .id(77L).caretakerId(CARETAKER_ID).serviceId(SERVICE_ID)
                .scheduledDate(week2).scheduledTime(LocalTime.of(10, 0))
                .status(BookingStatusEnum.PENDING).build();
        when(bookingDao.findByCaretakerIdAndDateAndStatuses(eq(CARETAKER_ID), eq(week2), anyList()))
                .thenReturn(List.of(conflicting));

        BookingRequest request = baseRequest().repeatWeekly(true).occurrences(3).build();

        GenericException ex = assertThrows(GenericException.class, () -> bookingService.create(request));

        assertThat(ex).isNotNull();
        assertThat(ex.getExceptionCode()).isEqualTo(ExceptionCodeEnum.BOOKING_CONFLICT);
        // All-or-nothing: even though the first two occurrences validated fine, nothing
        // was ever persisted once the third failed.
        verify(bookingDao, never()).saveAll(any());
        verify(bookingDao, never()).save(any());
        verify(notificationService, never()).create(any(), any(), any(), any(), any());
    }

    // ---------------------------------------------------------------
    // cancelSeries authorization
    // ---------------------------------------------------------------

    @Test
    void cancelSeries_noBookingsFound_throwsBookingNotFound() {
        when(bookingDao.findByRecurringGroupId("group-x")).thenReturn(List.of());

        GenericException ex = assertThrows(GenericException.class,
                () -> bookingService.cancelSeries("group-x", FAMILY_USER_ID));

        assertThat(ex).isNotNull();
        assertThat(ex.getExceptionCode()).isEqualTo(ExceptionCodeEnum.BOOKING_NOT_FOUND);
    }

    @Test
    void cancelSeries_ownerCanCancel() throws GenericException {
        List<Booking> series = List.of(
                Booking.builder().id(1L).familyUserId(FAMILY_USER_ID).caretakerId(CARETAKER_ID).serviceId(SERVICE_ID)
                        .elderProfileId(ELDER_PROFILE_ID).status(BookingStatusEnum.PENDING).recurringGroupId("g1").build(),
                Booking.builder().id(2L).familyUserId(FAMILY_USER_ID).caretakerId(CARETAKER_ID).serviceId(SERVICE_ID)
                        .elderProfileId(ELDER_PROFILE_ID).status(BookingStatusEnum.PENDING).recurringGroupId("g1").build());
        when(bookingDao.findByRecurringGroupId("g1")).thenReturn(series);
        when(userDao.findById(eq(FAMILY_USER_ID), eq(true)))
                .thenReturn(User.builder().id(FAMILY_USER_ID).userType(UserTypeEnum.FAMILY_MEMBER).build());
        when(bookingDao.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<BookingResponse> result = bookingService.cancelSeries("g1", FAMILY_USER_ID);

        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(r -> assertThat(r.getStatus()).isEqualTo(BookingStatusEnum.CANCELLED));
    }

    @Test
    void cancelSeries_adminCanCancelEvenIfNotOwner() throws GenericException {
        Long adminId = 500L;
        List<Booking> series = List.of(
                Booking.builder().id(1L).familyUserId(FAMILY_USER_ID).caretakerId(CARETAKER_ID).serviceId(SERVICE_ID)
                        .elderProfileId(ELDER_PROFILE_ID).status(BookingStatusEnum.PENDING).recurringGroupId("g1").build());
        when(bookingDao.findByRecurringGroupId("g1")).thenReturn(series);
        when(userDao.findById(eq(adminId), eq(true)))
                .thenReturn(User.builder().id(adminId).userType(UserTypeEnum.ADMIN).build());
        when(bookingDao.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<BookingResponse> result = bookingService.cancelSeries("g1", adminId);

        assertThat(result).hasSize(1);
    }

    @Test
    void cancelSeries_strangerIsForbidden() {
        Long strangerId = 999L;
        List<Booking> series = List.of(
                Booking.builder().id(1L).familyUserId(FAMILY_USER_ID).caretakerId(CARETAKER_ID).serviceId(SERVICE_ID)
                        .elderProfileId(ELDER_PROFILE_ID).status(BookingStatusEnum.PENDING).recurringGroupId("g1").build());
        when(bookingDao.findByRecurringGroupId("g1")).thenReturn(series);
        when(userDao.findById(eq(strangerId), eq(true)))
                .thenReturn(User.builder().id(strangerId).userType(UserTypeEnum.FAMILY_MEMBER).build());

        GenericException ex = assertThrows(GenericException.class,
                () -> bookingService.cancelSeries("g1", strangerId));

        assertThat(ex).isNotNull();
        assertThat(ex.getExceptionCode()).isEqualTo(ExceptionCodeEnum.FORBIDDEN);
        verify(bookingDao, never()).saveAll(any());
    }
}
