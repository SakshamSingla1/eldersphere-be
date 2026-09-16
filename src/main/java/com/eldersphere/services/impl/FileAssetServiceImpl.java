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

import java.util.Set;

@Service
@RequiredArgsConstructor
public class FileAssetServiceImpl implements FileAssetService {

    private final FileStorageService fileStorageService;
    private final FileAssetDao fileAssetDao;

    // Word/Excel MIME types the older binary (.doc/.xls) and modern OOXML (.docx/.xlsx)
    // formats each report as - browsers and upload widgets are inconsistent about which
    // one they send, so both are allowed for each format.
    private static final Set<String> ALLOWED_DOCUMENT_MIME_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    // Common browser/mobile-recorder video containers - deliberately broad since callers
    // (caretaker verification documents, medical records, chat attachments) may receive a
    // clip from any device.
    private static final Set<String> ALLOWED_VIDEO_MIME_TYPES = Set.of(
            "video/mp4", "video/webm", "video/quicktime", "video/ogg",
            "video/x-msvideo", "video/x-ms-wmv", "video/mpeg", "video/3gpp",
            "video/3gpp2", "video/x-flv", "video/x-matroska"
    );

    @Override
    @Transactional
    public FileAssetResponse upload(MultipartFile file, ResourceTypeEnum resourceType, Long uploadedBy) throws GenericException {
        validateMimeType(file.getContentType());
        FileStorageService.StoredFile stored = fileStorageService.store(file, resourceType);
        FileAsset asset = FileAsset.builder()
                .path(stored.path())
                .url(stored.url())
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .resourceType(resourceType)
                .uploadedBy(uploadedBy)
                .build();
        return toResponse(fileAssetDao.save(asset));
    }

    /** Allows images, PDFs, Word/Excel documents, and video - rejects everything else (e.g. raw executables, archives). */
    private void validateMimeType(String mimeType) throws GenericException {
        boolean allowed = mimeType != null
                && (mimeType.startsWith("image/") || ALLOWED_DOCUMENT_MIME_TYPES.contains(mimeType) || ALLOWED_VIDEO_MIME_TYPES.contains(mimeType));
        if (!allowed) {
            throw new GenericException(ExceptionCodeEnum.FILE_TYPE_NOT_ALLOWED,
                    "Unsupported file type: " + mimeType + ". Only images, PDF, Word, Excel, and video files are allowed.");
        }
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
