package com.eldersphere.dtos.MedicalRecord;

import com.eldersphere.dtos.Common.AuditableResponse;
import com.eldersphere.enums.MedicalRecordTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MedicalRecordResponse extends AuditableResponse {
    private Long id;
    private Long elderProfileId;
    private MedicalRecordTypeEnum type;
    private String title;
    private Long documentFileAssetId;
    private String documentUrl;
    private String notes;
    private boolean sharedWithFamily;
    private Long createdBy;
}
