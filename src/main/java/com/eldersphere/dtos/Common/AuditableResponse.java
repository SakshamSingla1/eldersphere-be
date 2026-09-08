package com.eldersphere.dtos.Common;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditableResponse {
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
