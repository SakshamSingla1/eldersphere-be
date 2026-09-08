package com.eldersphere.entities;

import com.eldersphere.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "platform_settings")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformSettings extends Auditable {

    @Id
    private Long id;

    @Column(name = "platform_name")
    private String platformName;

    @Column(name = "support_email")
    private String supportEmail;

    @Column(name = "support_phone")
    private String supportPhone;

    @Column(name = "emergency_response_sla_minutes")
    private Integer emergencyResponseSlaMinutes;
}
