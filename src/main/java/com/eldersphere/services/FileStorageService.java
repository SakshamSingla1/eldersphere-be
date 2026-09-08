package com.eldersphere.services;

import com.eldersphere.exceptions.GenericException;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * Saves the file to local disk under the configured upload directory and returns
     * the relative path it was stored at (e.g. "2026/09/uuid.jpg").
     */
    String store(MultipartFile file) throws GenericException;

    /** Builds the publicly-reachable URL for a previously stored relative path. */
    String buildPublicUrl(String relativePath);

    void delete(String relativePath);
}
