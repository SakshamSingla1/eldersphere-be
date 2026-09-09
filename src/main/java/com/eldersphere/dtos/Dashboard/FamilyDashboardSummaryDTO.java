package com.eldersphere.dtos.Dashboard;

import com.eldersphere.dtos.Elder.InviteSummaryDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FamilyDashboardSummaryDTO {
    private long managedElderCount;
    private long coManagedElderCount;
    private long upcomingBookings;
    private long unreadNotifications;
    private long pendingInviteCount;
    private List<InviteSummaryDTO> pendingInvites;
    private List<ActivityDTO> recentActivities;
}
