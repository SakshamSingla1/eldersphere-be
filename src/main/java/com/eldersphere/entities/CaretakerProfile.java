package com.eldersphere.entities;

import com.eldersphere.audit.Auditable;
import com.eldersphere.enums.CaretakerVerificationStatusEnum;
import com.eldersphere.enums.ServiceCategoryEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "caretaker_profiles")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaretakerProfile extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Builder.Default
    @ElementCollection(targetClass = ServiceCategoryEnum.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "caretaker_specialties", joinColumns = @JoinColumn(name = "caretaker_profile_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "specialty")
    private Set<ServiceCategoryEnum> specialties = new HashSet<>();

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "hourly_rate")
    private BigDecimal hourlyRate;

    @Column(name = "rating_average")
    private Double ratingAverage;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status")
    private CaretakerVerificationStatusEnum verificationStatus;

    @Column(name = "profile_photo_file_asset_id")
    private Long profilePhotoFileAssetId;

    @Column(name = "service_area")
    private String serviceArea;
}
