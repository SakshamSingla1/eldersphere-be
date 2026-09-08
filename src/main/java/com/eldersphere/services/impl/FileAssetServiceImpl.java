package com.eldersphere.services.impl;

import com.eldersphere.dao.file.FileAssetDao;
import com.eldersphere.dtos.File.FileAssetResponse;
import com.eldersphere.entities.FileAsset;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.enums.ResourceTypeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.services.FileAssetService;
import com.eldersphere.services.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileAssetServiceImpl implements FileAssetService {

    private final FileStorageService fileStorageService;
    private final FileAssetDao fileAssetDao;

    @Override
    @Transactional
    public FileAssetResponse upload(MultipartFile file, ResourceTypeEnum resourceType, Long uploadedBy) throws GenericException {
        String relativePath = fileStorageService.store(file);
        FileAsset asset = FileAsset.builder()
                .path(relativePath)
                .url(fileStorageService.buildPublicUrl(relativePath))
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .resourceType(resourceType)
                .uploadedBy(uploadedBy)
                .build();
        return toResponse(fileAssetDao.save(asset));
    }

    @Override
    public FileAssetResponse getById(Long id) throws GenericException {
        FileAsset asset = fileAssetDao.findById(id, true);
        if (asset == null) {
            throw new GenericException(ExceptionCodeEnum.FILE_NOT_FOUND, "File not found");
        }
        return toResponse(asset);
    }

    @Override
    @Transactional
    public void delete(Long id) throws GenericException {
        FileAsset asset = fileAssetDao.findById(id, true);
        if (asset == null) {
            throw new GenericException(ExceptionCodeEnum.FILE_NOT_FOUND, "File not found");
        }
        fileStorageService.delete(asset.getPath());
        fileAssetDao.deleteById(id);
    }

    private FileAssetResponse toResponse(FileAsset asset) {
        return FileAssetResponse.builder()
                .id(asset.getId())
                .url(asset.getUrl())
                .fileName(asset.getFileName())
                .fileType(asset.getFileType())
                .resourceType(asset.getResourceType())
                .uploadedBy(asset.getUploadedBy())
                .build();
    }
}
