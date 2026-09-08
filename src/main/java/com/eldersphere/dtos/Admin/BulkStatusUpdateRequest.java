package com.eldersphere.dtos.Admin;

import com.eldersphere.enums.UserStatusEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BulkStatusUpdateRequest {
    @NotEmpty(message = "At least one user ID is required")
    private List<Long> userIds;

    @NotNull(message = "Status is required")
    private UserStatusEnum status;
}
