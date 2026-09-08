package com.eldersphere.dtos.Caretaker;

import com.eldersphere.enums.ServiceCategoryEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
public class CaretakerProfileRequest {
    private Long userId;
    private String bio;
    private Set<ServiceCategoryEnum> specialties;

    @Min(value = 0, message = "Years of experience cannot be negative")
    private Integer yearsOfExperience;

    @DecimalMin(value = "0.0", message = "Hourly rate cannot be negative")
    private BigDecimal hourlyRate;

    private String serviceArea;
}
