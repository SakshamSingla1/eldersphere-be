package com.eldersphere.dtos.Elder;

import com.eldersphere.dtos.Common.AuditableResponse;
import com.eldersphere.enums.LinkInviteStatusEnum;
import com.eldersphere.enums.UserTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class InviteResponse extends AuditableResponse {
    private Long id;
    private Long elderProfileId;
    private String elderName;
    private Long invitedUserId;
    private String invitedUserName;
    private UserTypeEnum invitedRole;
    private Long invitedByUserId;
    private String invitedByName;
    private String relationshipLabel;
    private LinkInviteStatusEnum status;
    private LocalDateTime respondedAt;
}
