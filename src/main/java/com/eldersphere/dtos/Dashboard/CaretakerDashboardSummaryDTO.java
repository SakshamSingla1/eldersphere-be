package com.eldersphere.dtos.Dashboard;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CaretakerDashboardSummaryDTO {
    private long upcomingBookings;
    private long completedBookings;
    private Double averageRating;
    private long unreadNotifications;
    private List<ActivityDTO> recentActivities;
}
