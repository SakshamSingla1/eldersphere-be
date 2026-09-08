package com.eldersphere.dao.medicalrecord;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.MedicalRecord;
import com.eldersphere.repositories.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MedicalRecordDao implements IDao<MedicalRecord, Long> {

    private final MedicalRecordRepository medicalRecordRepository;

    @Override
    public JpaRepository<MedicalRecord, Long> getRepository() {
        return medicalRecordRepository;
    }

    public MedicalRecord save(MedicalRecord record) {
        return medicalRecordRepository.save(record);
    }

    public Page<MedicalRecord> findByElderProfileId(Long elderProfileId, Pageable pageable) {
        return medicalRecordRepository.findByElderProfileId(elderProfileId, pageable);
    }

    public Page<MedicalRecord> findSharedByElderProfileId(Long elderProfileId, Pageable pageable) {
        return medicalRecordRepository.findByElderProfileIdAndSharedWithFamilyTrue(elderProfileId, pageable);
    }

    public void deleteById(Long id) {
        medicalRecordRepository.deleteById(id);
    }
}
