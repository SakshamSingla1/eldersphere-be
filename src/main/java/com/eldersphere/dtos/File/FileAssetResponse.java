package com.eldersphere.dtos.File;

import com.eldersphere.enums.ResourceTypeEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileAssetResponse {
    private Long id;
    private String url;
    private String fileName;
    private String fileType;
    private ResourceTypeEnum resourceType;
    private Long uploadedBy;
}
