package com.eldersphere.dtos.Admin;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BulkUserIdsRequest {
    @NotEmpty(message = "At least one user ID is required")
    private List<Long> userIds;
}
