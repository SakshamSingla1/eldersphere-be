package com.eldersphere.repositories;

import com.eldersphere.entities.Payment;
import com.eldersphere.enums.PaymentStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);

    List<Payment> findByBookingIdOrderByCreatedAtDesc(Long bookingId);

    Optional<Payment> findFirstByBookingIdAndStatusOrderByCreatedAtDesc(Long bookingId, PaymentStatusEnum status);

    @Query("""
            SELECT p FROM Payment p
            WHERE (:bookingId IS NULL OR p.bookingId = :bookingId)
            AND (:familyUserId IS NULL OR p.familyUserId = :familyUserId)
            AND (:status IS NULL OR p.status = :status)
            """)
    Page<Payment> findByCriteria(@Param("bookingId") Long bookingId,
                                  @Param("familyUserId") Long familyUserId,
                                  @Param("status") PaymentStatusEnum status,
                                  Pageable pageable);

    @Query(value = """
            SELECT COALESCE(SUM(p.amount), 0)
            FROM payments p
            JOIN bookings b ON b.id = p.booking_id
            WHERE b.caretaker_id = :caretakerId AND p.status = 'SUCCEEDED'
            """, nativeQuery = true)
    BigDecimal sumSucceededByCaretakerId(@Param("caretakerId") Long caretakerId);

    @Query(value = """
            SELECT COUNT(*)
            FROM payments p
            JOIN bookings b ON b.id = p.booking_id
            WHERE b.caretaker_id = :caretakerId AND p.status = 'SUCCEEDED'
            """, nativeQuery = true)
    long countSucceededByCaretakerId(@Param("caretakerId") Long caretakerId);

    @Query(value = """
            SELECT CAST(date_trunc('month', p.paid_at) AS date) AS bucket,
                   COALESCE(SUM(p.amount), 0) AS total
            FROM payments p
            JOIN bookings b ON b.id = p.booking_id
            WHERE b.caretaker_id = :caretakerId AND p.status = 'SUCCEEDED'
            GROUP BY bucket
            ORDER BY bucket
            """, nativeQuery = true)
    List<Object[]> monthlyEarningsRaw(@Param("caretakerId") Long caretakerId);

    @Query(value = """
            SELECT CAST(date_trunc('month', p.paid_at) AS date) AS bucket,
                   COALESCE(SUM(p.amount), 0) AS total
            FROM payments p
            WHERE p.family_user_id = :familyUserId AND p.status = 'SUCCEEDED'
            GROUP BY bucket
            ORDER BY bucket
            """, nativeQuery = true)
    List<Object[]> monthlySpendingRaw(@Param("familyUserId") Long familyUserId);
}
