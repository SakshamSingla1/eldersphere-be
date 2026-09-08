package com.eldersphere.entities;

import com.eldersphere.audit.Auditable;
import com.eldersphere.enums.BookingStatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "bookings")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "family_user_id", nullable = false)
    private Long familyUserId;

    @Column(name = "elder_profile_id", nullable = false)
    private Long elderProfileId;

    @Column(name = "caretaker_id", nullable = false)
    private Long caretakerId;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    @Column(name = "scheduled_time")
    private LocalTime scheduledTime;

    @Enumerated(EnumType.STRING)
    private BookingStatusEnum status;

    private BigDecimal cost;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "recurring_group_id", length = 36)
    private String recurringGroupId;
}
