package com.eldersphere.repositories;

import com.eldersphere.entities.FileAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileAssetRepository extends JpaRepository<FileAsset, Long> {
}
