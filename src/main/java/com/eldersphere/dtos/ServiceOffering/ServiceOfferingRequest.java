package com.eldersphere.dtos.ServiceOffering;

import com.eldersphere.enums.ServiceCategoryEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ServiceOfferingRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Category is required")
    private ServiceCategoryEnum category;

    private String description;

    @DecimalMin(value = "0.0", message = "Base price cannot be negative")
    private BigDecimal basePrice;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;
}
