package com.eldersphere.dtos.Caretaker;

import com.eldersphere.dtos.Common.AuditableResponse;
import com.eldersphere.enums.CaretakerVerificationStatusEnum;
import com.eldersphere.enums.ServiceCategoryEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class CaretakerProfileResponse extends AuditableResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String bio;
    private Set<ServiceCategoryEnum> specialties;
    private Integer yearsOfExperience;
    private BigDecimal hourlyRate;
    private Double ratingAverage;
    private CaretakerVerificationStatusEnum verificationStatus;
    private Long profilePhotoFileAssetId;
    private String profilePhotoUrl;
    private String serviceArea;
}
