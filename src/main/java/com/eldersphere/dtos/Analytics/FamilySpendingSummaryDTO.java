package com.eldersphere.dtos.Analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilySpendingSummaryDTO {
    private List<RevenueTimeseriesPointDTO> spendingOverTime;
    /** Count per BookingStatusEnum name, e.g. {"COMPLETED": 4, "CANCELLED": 1}. */
    private Map<String, Long> bookingsByStatus;
}
