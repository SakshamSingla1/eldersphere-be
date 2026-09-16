package com.eldersphere.services;

import com.eldersphere.enums.ResourceTypeEnum;
import com.eldersphere.exceptions.GenericException;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * Result of a store() call: {@code path} is an opaque key this service later needs back
     * for {@link #delete}, and {@code url} is the publicly-reachable download URL to persist
     * on the {@code FileAsset} row.
     */
    record StoredFile(String path, String url) {}

    /**
     * Stores the file - on Cloudinary when configured (see CloudinaryConfig), otherwise on
     * local disk under the configured upload directory - organized under a
     * {@code resourceType}-named folder either way.
     */
    StoredFile store(MultipartFile file, ResourceTypeEnum resourceType) throws GenericException;

    /** Deletes a previously stored file, given the {@code path} its store() call returned. */
    void delete(String path);
}
