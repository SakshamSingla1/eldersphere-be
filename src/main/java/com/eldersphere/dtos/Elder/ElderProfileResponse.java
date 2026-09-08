package com.eldersphere.dtos.Elder;

import com.eldersphere.dtos.Common.AuditableResponse;
import com.eldersphere.enums.GenderEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class ElderProfileResponse extends AuditableResponse {
    private Long id;
    private Long familyUserId;
    private Long elderUserId;
    private String name;
    private LocalDate dateOfBirth;
    private GenderEnum gender;
    private String medicalConditions;
    private String address;
    private String emergencyContactName;
    private String emergencyContactPhone;
}
