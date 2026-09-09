package com.eldersphere.dtos.Elder;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** Compact shape used by dashboard summaries to surface a family/elder's pending invites. */
@Data
@Builder
public class InviteSummaryDTO {
    private Long id;
    private Long elderProfileId;
    private String elderName;
    private String invitedByName;
    private String relationshipLabel;
    private LocalDateTime createdAt;
}
