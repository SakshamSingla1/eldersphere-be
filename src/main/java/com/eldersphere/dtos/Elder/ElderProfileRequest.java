package com.eldersphere.dtos.Elder;

import com.eldersphere.enums.GenderEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ElderProfileRequest {
    private Long familyUserId;
    private Long elderUserId;

    @NotBlank(message = "Name is required")
    private String name;

    private LocalDate dateOfBirth;
    private GenderEnum gender;
    private String medicalConditions;
    private String address;
    private String emergencyContactName;
    private String emergencyContactPhone;
}
