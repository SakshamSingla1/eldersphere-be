package com.eldersphere.dtos.ServiceOffering;

import com.eldersphere.dtos.Common.AuditableResponse;
import com.eldersphere.enums.ServiceCategoryEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceOfferingResponse extends AuditableResponse {
    private Long id;
    private String name;
    private ServiceCategoryEnum category;
    private String description;
    private BigDecimal basePrice;
    private Integer durationMinutes;
}
