package com.eldersphere.dtos.Analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingTimeseriesPointDTO {
    private LocalDate bucketStart;
    private long total;
    /** Count per BookingStatusEnum name, e.g. {"COMPLETED": 4, "CANCELLED": 1}. */
    private Map<String, Long> byStatus;
}
