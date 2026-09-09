package com.eldersphere.dtos.Emergency;

import com.eldersphere.dtos.Common.AuditableResponse;
import com.eldersphere.enums.EmergencyAlertStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class EmergencyAlertResponse extends AuditableResponse {
    private Long id;
    private Long elderProfileId;
    private String elderName;
    private Long triggeredByUserId;
    private Double latitude;
    private Double longitude;
    private String resolvedAddress;
    private EmergencyAlertStatusEnum status;
    private Long respondingCaretakerId;
    private LocalDateTime triggeredAt;
    private LocalDateTime resolvedAt;
    private Integer responseTimeSeconds;
}
