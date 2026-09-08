package com.eldersphere.dtos.Platform;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlatformSettingsDTO {
    private String platformName;
    private String supportEmail;
    private String supportPhone;
    private Integer emergencyResponseSlaMinutes;
}
