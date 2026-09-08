package com.eldersphere.dtos.Admin;

import com.eldersphere.enums.UserStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatusUpdateRequest {
    @NotNull(message = "Status is required")
    private UserStatusEnum status;
}
