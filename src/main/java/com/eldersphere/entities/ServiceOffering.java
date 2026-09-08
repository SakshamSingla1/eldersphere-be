package com.eldersphere.entities;

import com.eldersphere.audit.Auditable;
import com.eldersphere.enums.ServiceCategoryEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "service_offerings")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOffering extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private ServiceCategoryEnum category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_price")
    private BigDecimal basePrice;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;
}
