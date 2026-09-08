package com.eldersphere.services;

import com.eldersphere.dtos.File.FileAssetResponse;
import com.eldersphere.enums.ResourceTypeEnum;
import com.eldersphere.exceptions.GenericException;
import org.springframework.web.multipart.MultipartFile;

public interface FileAssetService {
    FileAssetResponse upload(MultipartFile file, ResourceTypeEnum resourceType, Long uploadedBy) throws GenericException;
    FileAssetResponse getById(Long id) throws GenericException;
    void delete(Long id) throws GenericException;
}
