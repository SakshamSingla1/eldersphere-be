package com.eldersphere.repositories;

import com.eldersphere.entities.Booking;
import com.eldersphere.enums.BookingStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByFamilyUserId(Long familyUserId, Pageable pageable);

    Page<Booking> findByCaretakerId(Long caretakerId, Pageable pageable);

    @Query("""
            SELECT b FROM Booking b
            WHERE (:familyUserId IS NULL OR b.familyUserId = :familyUserId)
            AND (:caretakerId IS NULL OR b.caretakerId = :caretakerId)
            AND (:status IS NULL OR b.status = :status)
            """)
    Page<Booking> findByCriteria(@Param("familyUserId") Long familyUserId,
                                  @Param("caretakerId") Long caretakerId,
                                  @Param("status") BookingStatusEnum status,
                                  Pageable pageable);

    long countByStatus(BookingStatusEnum status);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(b.cost), 0) FROM Booking b WHERE b.status = 'COMPLETED' AND b.createdAt BETWEEN :start AND :end")
    BigDecimal sumRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    boolean existsByCaretakerIdAndScheduledDateAndScheduledTimeAndStatusIn(
            Long caretakerId, LocalDate scheduledDate, java.time.LocalTime scheduledTime, List<BookingStatusEnum> statuses);

    List<Booking> findByCaretakerIdAndScheduledDateAndStatusIn(
            Long caretakerId, LocalDate scheduledDate, List<BookingStatusEnum> statuses);

    List<Booking> findByRecurringGroupIdOrderByScheduledDateAsc(String recurringGroupId);

    List<Booking> findTop10ByOrderByCreatedAtDesc();

    List<Booking> findTop10ByFamilyUserIdOrderByCreatedAtDesc(Long familyUserId);

    List<Booking> findTop10ByCaretakerIdOrderByCreatedAtDesc(Long caretakerId);

    long countByFamilyUserIdAndStatusIn(Long familyUserId, List<BookingStatusEnum> statuses);

    long countByCaretakerIdAndStatusIn(Long caretakerId, List<BookingStatusEnum> statuses);

    long countByCaretakerIdAndStatus(Long caretakerId, BookingStatusEnum status);

    long countByElderProfileIdAndStatusIn(Long elderProfileId, List<BookingStatusEnum> statuses);

    /**
     * Row shape: [bucket_date (java.sql.Date), status (String), count (Long)].
     * {@code unit} is passed straight to Postgres' {@code date_trunc} ('day' or 'week').
     */
    @Query(value = """
            SELECT CAST(date_trunc(:unit, b.scheduled_date::timestamp) AS date) AS bucket,
                   b.status AS status,
                   COUNT(*) AS cnt
            FROM bookings b
            WHERE b.scheduled_date BETWEEN :start AND :end
            GROUP BY bucket, b.status
            ORDER BY bucket
            """, nativeQuery = true)
    List<Object[]> bookingTimeseriesRaw(@Param("unit") String unit,
                                         @Param("start") LocalDate start,
                                         @Param("end") LocalDate end);

    /**
     * Row shape: [bucket_date (java.sql.Date), revenue (BigDecimal)]. Only COMPLETED bookings.
     */
    @Query(value = """
            SELECT CAST(date_trunc(:unit, b.scheduled_date::timestamp) AS date) AS bucket,
                   COALESCE(SUM(b.cost), 0) AS revenue
            FROM bookings b
            WHERE b.status = 'COMPLETED' AND b.scheduled_date BETWEEN :start AND :end
            GROUP BY bucket
            ORDER BY bucket
            """, nativeQuery = true)
    List<Object[]> revenueTimeseriesRaw(@Param("unit") String unit,
                                         @Param("start") LocalDate start,
                                         @Param("end") LocalDate end);
}
