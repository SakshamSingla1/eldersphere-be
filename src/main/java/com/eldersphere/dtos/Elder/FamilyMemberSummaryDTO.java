package com.eldersphere.dtos.Elder;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FamilyMemberSummaryDTO {
    private Long userId;
    private String fullName;
    private String relationshipLabel;
    private boolean isOwner;
}
