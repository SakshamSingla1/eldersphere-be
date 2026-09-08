package com.eldersphere.dtos.Dashboard;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ElderDashboardSummaryDTO {
    private long upcomingBookings;
    private long activeEmergencyAlerts;
    private long unreadNotifications;
    private List<ActivityDTO> recentMedicalRecords;
}
