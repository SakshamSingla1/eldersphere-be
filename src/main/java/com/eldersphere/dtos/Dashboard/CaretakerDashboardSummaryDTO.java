package com.eldersphere.dtos.Dashboard;

import com.eldersphere.enums.CaretakerVerificationStatusEnum;
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
    private CaretakerVerificationStatusEnum verificationStatus;
    private boolean hasAvailabilitySet;
    private List<ActivityDTO> recentActivities;
}
