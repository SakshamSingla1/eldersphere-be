package com.eldersphere.dtos.Emergency;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmergencyAlertRequest {
    @NotNull(message = "Elder profile is required")
    private Long elderProfileId;

    private Double latitude;
    private Double longitude;
}
