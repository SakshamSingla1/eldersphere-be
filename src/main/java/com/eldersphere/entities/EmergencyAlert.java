package com.eldersphere.entities;

import com.eldersphere.audit.Auditable;
import com.eldersphere.enums.EmergencyAlertStatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_alerts")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyAlert extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "elder_profile_id", nullable = false)
    private Long elderProfileId;

    @Column(name = "triggered_by_user_id", nullable = false)
    private Long triggeredByUserId;

    private Double latitude;

    private Double longitude;

    @Column(name = "resolved_address", columnDefinition = "TEXT")
    private String resolvedAddress;

    @Enumerated(EnumType.STRING)
    private EmergencyAlertStatusEnum status;

    @Column(name = "responding_caretaker_id")
    private Long respondingCaretakerId;

    @Column(name = "triggered_at")
    private LocalDateTime triggeredAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "response_time_seconds")
    private Integer responseTimeSeconds;
}
