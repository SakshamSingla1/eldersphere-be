package com.eldersphere.dtos.Common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GeocodingResultDTO {
    private String displayName;
    private Double latitude;
    private Double longitude;
}
