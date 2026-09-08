package com.eldersphere.dtos.MedicalRecord;

import com.eldersphere.enums.MedicalRecordTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MedicalRecordRequest {
    @NotNull(message = "Elder profile is required")
    private Long elderProfileId;

    @NotNull(message = "Type is required")
    private MedicalRecordTypeEnum type;

    @NotBlank(message = "Title is required")
    private String title;

    private Long documentFileAssetId;
    private String notes;
    private boolean sharedWithFamily;
}
