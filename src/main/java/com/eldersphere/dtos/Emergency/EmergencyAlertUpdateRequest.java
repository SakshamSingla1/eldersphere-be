package com.eldersphere.dtos.Emergency;

import com.eldersphere.enums.EmergencyAlertStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmergencyAlertUpdateRequest {
    @NotNull(message = "Status is required")
    private EmergencyAlertStatusEnum status;

    private Long respondingCaretakerId;
}
