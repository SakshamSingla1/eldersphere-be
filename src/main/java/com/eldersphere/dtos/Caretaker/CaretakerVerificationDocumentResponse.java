package com.eldersphere.dtos.Caretaker;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CaretakerVerificationDocumentResponse {
    private Long id;
    private Long caretakerProfileId;
    private Long fileAssetId;
    private String fileName;
    private String fileType;
    private String url;
    private Long uploadedBy;
    private LocalDateTime createdAt;
}
