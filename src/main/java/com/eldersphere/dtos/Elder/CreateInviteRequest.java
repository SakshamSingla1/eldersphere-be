package com.eldersphere.dtos.Elder;

import com.eldersphere.enums.UserTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateInviteRequest {

    @NotNull(message = "targetUserId is required")
    private Long targetUserId;

    @NotNull(message = "invitedRole is required")
    private UserTypeEnum invitedRole;

    private String relationshipLabel;
}
