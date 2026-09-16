package com.eldersphere.dao.booking;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.Booking;
import com.eldersphere.enums.BookingStatusEnum;
import com.eldersphere.repositories.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BookingDao implements IDao<Booking, Long> {

    private final BookingRepository bookingRepository;

    @Override
    public JpaRepository<Booking, Long> getRepository() {
        return bookingRepository;
    }

    public Booking save(Booking booking) {
        return bookingRepository.save(booking);
    }

    public Page<Booking> findByCriteria(Long familyUserId, Long caretakerId, BookingStatusEnum status, Pageable pageable) {
        return bookingRepository.findByCriteria(familyUserId, caretakerId, status, pageable);
    }

    public long countByStatus(BookingStatusEnum status) {
        return bookingRepository.countByStatus(status);
    }

    public long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end) {
        return bookingRepository.countByCreatedAtBetween(start, end);
    }

    public BigDecimal sumRevenueBetween(LocalDateTime start, LocalDateTime end) {
        return bookingRepository.sumRevenueBetween(start, end);
    }

    public boolean existsConflict(Long caretakerId, LocalDate date, LocalTime time, List<BookingStatusEnum> activeStatuses) {
        return bookingRepository.existsByCaretakerIdAndScheduledDateAndScheduledTimeAndStatusIn(caretakerId, date, time, activeStatuses);
    }

    public List<Booking> findByCaretakerIdAndDateAndStatuses(Long caretakerId, LocalDate date, List<BookingStatusEnum> statuses) {
        return bookingRepository.findByCaretakerIdAndScheduledDateAndStatusIn(caretakerId, date, statuses);
    }

    public List<Booking> findByRecurringGroupId(String recurringGroupId) {
        return bookingRepository.findByRecurringGroupIdOrderByScheduledDateAsc(recurringGroupId);
    }

    public List<Booking> saveAll(List<Booking> bookings) {
        return bookingRepository.saveAll(bookings);
    }

    public List<Booking> findRecent() {
        return bookingRepository.findTop10ByOrderByCreatedAtDesc();
    }

    public List<Booking> findRecentByFamilyUserId(Long familyUserId) {
        return bookingRepository.findTop10ByFamilyUserIdOrderByCreatedAtDesc(familyUserId);
    }

    public List<Booking> findRecentByCaretakerId(Long caretakerId) {
        return bookingRepository.findTop10ByCaretakerIdOrderByCreatedAtDesc(caretakerId);
    }

    public long countByFamilyUserIdAndStatusIn(Long familyUserId, List<BookingStatusEnum> statuses) {
        return bookingRepository.countByFamilyUserIdAndStatusIn(familyUserId, statuses);
    }

    public long countByCaretakerIdAndStatusIn(Long caretakerId, List<BookingStatusEnum> statuses) {
        return bookingRepository.countByCaretakerIdAndStatusIn(caretakerId, statuses);
    }

    public long countByCaretakerIdAndStatus(Long caretakerId, BookingStatusEnum status) {
        return bookingRepository.countByCaretakerIdAndStatus(caretakerId, status);
    }

    public long countByElderProfileIdAndStatusIn(Long elderProfileId, List<BookingStatusEnum> statuses) {
        return bookingRepository.countByElderProfileIdAndStatusIn(elderProfileId, statuses);
    }

    public List<Object[]> bookingTimeseriesRaw(String unit, LocalDate start, LocalDate end) {
        return bookingRepository.bookingTimeseriesRaw(unit, start, end);
    }

    public List<Object[]> revenueTimeseriesRaw(String unit, LocalDate start, LocalDate end) {
        return bookingRepository.revenueTimeseriesRaw(unit, start, end);
    }

    public List<Object[]> bookingStatusCountsForFamilyUser(Long familyUserId) {
        return bookingRepository.bookingStatusCountsForFamilyUser(familyUserId);
    }

    public List<Object[]> weeklyCompletedBookingsRaw(Long caretakerId) {
        return bookingRepository.weeklyCompletedBookingsRaw(caretakerId);
    }
}
