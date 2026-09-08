package com.eldersphere.dtos.Dashboard;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FamilyDashboardSummaryDTO {
    private long managedElderCount;
    private long upcomingBookings;
    private long unreadNotifications;
    private List<ActivityDTO> recentActivities;
}
