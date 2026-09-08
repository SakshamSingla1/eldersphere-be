package com.eldersphere.services.impl;

import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.services.FileStorageService;
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
import java.util.UUID;

/**
 * Stores uploaded files on local disk under app.file-storage.upload-dir, organized by
 * yyyy/MM subfolders. This replaces the reference project's Cloudinary integration —
 * see README "Scope decisions" for why.
 */
@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.file-storage.upload-dir}")
    private String uploadDir;

    @Value("${app.file-storage.public-base-url}")
    private String publicBaseUrl;

    @Override
    public String store(MultipartFile file) throws GenericException {
        if (file == null || file.isEmpty()) {
            throw new GenericException(ExceptionCodeEnum.BAD_REQUEST, "File is empty");
        }
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

            return subDir + "/" + storedName;
        } catch (IOException e) {
            log.error("Failed to store file: {}", e.getMessage());
            throw new GenericException(ExceptionCodeEnum.FILE_STORAGE_FAILED, "Failed to store file");
        }
    }

    @Override
    public String buildPublicUrl(String relativePath) {
        if (relativePath == null) return null;
        return publicBaseUrl + "/" + relativePath;
    }

    @Override
    public void delete(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) return;
        try {
            Files.deleteIfExists(Paths.get(uploadDir, relativePath));
        } catch (IOException e) {
            log.warn("Failed to delete file {}: {}", relativePath, e.getMessage());
        }
    }
}
