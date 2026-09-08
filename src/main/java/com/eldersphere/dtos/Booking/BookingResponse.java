package com.eldersphere.dtos.Booking;

import com.eldersphere.dtos.Common.AuditableResponse;
import com.eldersphere.enums.BookingStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class BookingResponse extends AuditableResponse {
    private Long id;
    private Long familyUserId;
    private Long elderProfileId;
    private String elderName;
    private Long caretakerId;
    private String caretakerName;
    private Long serviceId;
    private String serviceName;
    private LocalDate scheduledDate;
    private LocalTime scheduledTime;
    private BookingStatusEnum status;
    private BigDecimal cost;
    private String notes;
    private String recurringGroupId;
}
