package com.eldersphere.services.impl;

import com.cloudinary.utils.ObjectUtils;
import com.eldersphere.config.CloudinaryConfig;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.enums.ResourceTypeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.services.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

/**
 * Stores uploaded files on Cloudinary when configured (see {@link CloudinaryConfig}),
 * organized under an {@code eldersphere/<resourceType>} folder; otherwise falls back to
 * local disk under {@code app.file-storage.upload-dir} (yyyy/MM subfolders), served via a
 * Spring ResourceHandler at {@code /uploads/**}. The two modes are distinguished purely by
 * the {@code path} format returned from {@link #store}: a Cloudinary-stored path is prefixed
 * {@code "cloudinary:<resourceType>:"} (needed by {@link #delete} to call the right API);
 * anything else - including every path already sitting in the database from before this
 * class supported Cloudinary - is treated as a local-disk-relative path, so no data
 * migration is required when Cloudinary credentials are added later.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private static final String CLOUDINARY_PREFIX = "cloudinary:";

    private final CloudinaryConfig cloudinaryConfig;

    @Value("${app.file-storage.upload-dir}")
    private String uploadDir;

    @Value("${app.file-storage.public-base-url}")
    private String publicBaseUrl;

    @Override
    public StoredFile store(MultipartFile file, ResourceTypeEnum resourceType) throws GenericException {
        if (file == null || file.isEmpty()) {
            throw new GenericException(ExceptionCodeEnum.BAD_REQUEST, "File is empty");
        }
        return cloudinaryConfig.isConfigured() ? storeToCloudinary(file, resourceType) : storeToLocalDisk(file);
    }

    @Override
    public void delete(String path) {
        if (path == null || path.isBlank()) return;
        if (path.startsWith(CLOUDINARY_PREFIX)) {
            deleteFromCloudinary(path);
        } else {
            deleteFromLocalDisk(path);
        }
    }

    // ====== Cloudinary ======

    private StoredFile storeToCloudinary(MultipartFile file, ResourceTypeEnum resourceType) throws GenericException {
        String cloudinaryResourceType = resolveCloudinaryResourceType(file.getContentType());
        String folder = "eldersphere/" + resourceType.name().toLowerCase();
        String publicId = folder + "/" + baseName(file.getOriginalFilename()) + "_" + UUID.randomUUID();
        try {
            Map<?, ?> result = cloudinaryConfig.getCloudinary().uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "public_id", publicId,
                    "resource_type", cloudinaryResourceType,
                    "overwrite", false
            ));
            String secureUrl = String.valueOf(result.get("secure_url"));
            String storedPublicId = String.valueOf(result.get("public_id"));
            String path = CLOUDINARY_PREFIX + cloudinaryResourceType + ":" + storedPublicId;
            return new StoredFile(path, secureUrl);
        } catch (IOException e) {
            log.error("Failed to upload file to Cloudinary: {}", e.getMessage());
            throw new GenericException(ExceptionCodeEnum.FILE_STORAGE_FAILED, "Failed to upload file to Cloudinary");
        }
    }

    private void deleteFromCloudinary(String path) {
        // path shape: "cloudinary:<resourceType>:<publicId>" - publicId itself may contain
        // ':' only in pathological cases (Cloudinary public_ids don't normally include it),
        // so splitting into at most 3 parts keeps any such id intact in the third segment.
        String[] parts = path.split(":", 3);
        if (parts.length != 3) {
            log.warn("Malformed Cloudinary file path, skipping delete: {}", path);
            return;
        }
        String cloudinaryResourceType = parts[1];
        String publicId = parts[2];
        try {
            cloudinaryConfig.getCloudinary().uploader().destroy(publicId, ObjectUtils.asMap("resource_type", cloudinaryResourceType));
        } catch (Exception e) {
            log.warn("Failed to delete Cloudinary file {}: {}", publicId, e.getMessage());
        }
    }

    /** Cloudinary buckets every upload into "image", "video", or "raw" (everything else - PDF, Word, Excel, ...). */
    private String resolveCloudinaryResourceType(String contentType) {
        if (contentType == null) return "raw";
        if (contentType.startsWith("image/")) return "image";
        if (contentType.startsWith("video/")) return "video";
        return "raw";
    }

    /** Strips the extension and sanitizes an uploaded filename for use inside a Cloudinary public_id. */
    private String baseName(String originalFilename) {
        String cleaned = StringUtils.cleanPath(originalFilename != null ? originalFilename : "file");
        int dotIndex = cleaned.lastIndexOf('.');
        String withoutExtension = dotIndex >= 0 ? cleaned.substring(0, dotIndex) : cleaned;
        return withoutExtension.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    // ====== Local disk ======

    private StoredFile storeToLocalDisk(MultipartFile file) throws GenericException {
        try {
            String subDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
            Path targetDir = Paths.get(uploadDir, subDir);
            Files.createDirectories(targetDir);

            String originalName = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
            String extension = "";
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex >= 0) {
                extension = originalName.substring(dotIndex);
            }
            String storedName = UUID.randomUUID() + extension;
            Path targetPath = targetDir.resolve(storedName);
            Files.copy(file.getInputStream(), targetPath);

            String relativePath = subDir + "/" + storedName;
            return new StoredFile(relativePath, publicBaseUrl + "/" + relativePath);
        } catch (IOException e) {
            log.error("Failed to store file: {}", e.getMessage());
            throw new GenericException(ExceptionCodeEnum.FILE_STORAGE_FAILED, "Failed to store file");
        }
    }

    private void deleteFromLocalDisk(String relativePath) {
        try {
            Files.deleteIfExists(Paths.get(uploadDir, relativePath));
        } catch (IOException e) {
            log.warn("Failed to delete file {}: {}", relativePath, e.getMessage());
        }
    }
}
