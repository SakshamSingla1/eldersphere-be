package com.eldersphere.dao.payment;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.Payment;
import com.eldersphere.enums.PaymentStatusEnum;
import com.eldersphere.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentDao implements IDao<Payment, Long> {

    private final PaymentRepository paymentRepository;

    @Override
    public JpaRepository<Payment, Long> getRepository() {
        return paymentRepository;
    }

    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }

    public Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId) {
        return paymentRepository.findByStripePaymentIntentId(stripePaymentIntentId);
    }

    public List<Payment> findByBookingId(Long bookingId) {
        return paymentRepository.findByBookingIdOrderByCreatedAtDesc(bookingId);
    }

    public Optional<Payment> findNonTerminalByBookingId(Long bookingId) {
        return paymentRepository.findFirstByBookingIdAndStatusOrderByCreatedAtDesc(bookingId, PaymentStatusEnum.PENDING);
    }

    public Page<Payment> search(Long bookingId, Long familyUserId, PaymentStatusEnum status, Pageable pageable) {
        return paymentRepository.findByCriteria(bookingId, familyUserId, status, pageable);
    }

    public BigDecimal sumSucceededByCaretakerId(Long caretakerId) {
        return paymentRepository.sumSucceededByCaretakerId(caretakerId);
    }

    public long countSucceededByCaretakerId(Long caretakerId) {
        return paymentRepository.countSucceededByCaretakerId(caretakerId);
    }

    public List<Object[]> monthlyEarningsRaw(Long caretakerId) {
        return paymentRepository.monthlyEarningsRaw(caretakerId);
    }

    public List<Object[]> monthlySpendingRaw(Long familyUserId) {
        return paymentRepository.monthlySpendingRaw(familyUserId);
    }
}
