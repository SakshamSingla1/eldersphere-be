package com.eldersphere.dtos.Search;

import com.eldersphere.enums.CaretakerVerificationStatusEnum;
import com.eldersphere.enums.ServiceCategoryEnum;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
public class CaretakerSearchResultDTO {
    private Long id;
    private Long userId;
    private String fullName;
    private String bio;
    private Set<ServiceCategoryEnum> specialties;
    private Integer yearsOfExperience;
    private BigDecimal hourlyRate;
    private Double ratingAverage;
    private CaretakerVerificationStatusEnum verificationStatus;
    private String profilePhotoUrl;
    private String serviceArea;
}
