package com.eldersphere.dao.file;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.FileAsset;
import com.eldersphere.repositories.FileAssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FileAssetDao implements IDao<FileAsset, Long> {

    private final FileAssetRepository fileAssetRepository;

    @Override
    public JpaRepository<FileAsset, Long> getRepository() {
        return fileAssetRepository;
    }

    public FileAsset save(FileAsset fileAsset) {
        return fileAssetRepository.save(fileAsset);
    }

    public void deleteById(Long id) {
        fileAssetRepository.deleteById(id);
    }
}
