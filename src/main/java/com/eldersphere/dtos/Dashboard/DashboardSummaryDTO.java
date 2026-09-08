package com.eldersphere.dtos.Dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DashboardSummaryDTO {
    private long totalBookings;
    private long activeCaretakers;
    private long pendingEmergencyAlerts;
    private long totalElders;
    private long totalFamilies;
    private BigDecimal revenueThisMonth;
    private BigDecimal revenueLast30Days;
    private List<ActivityDTO> recentActivities;
}
