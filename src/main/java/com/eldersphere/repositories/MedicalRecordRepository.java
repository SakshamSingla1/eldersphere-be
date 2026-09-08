package com.eldersphere.repositories;

import com.eldersphere.entities.MedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    Page<MedicalRecord> findByElderProfileId(Long elderProfileId, Pageable pageable);
    Page<MedicalRecord> findByElderProfileIdAndSharedWithFamilyTrue(Long elderProfileId, Pageable pageable);
}
