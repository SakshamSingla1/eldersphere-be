package com.eldersphere.services.impl;

import com.eldersphere.dao.file.FileAssetDao;
import com.eldersphere.dao.medicalrecord.MedicalRecordDao;
import com.eldersphere.dtos.MedicalRecord.MedicalRecordRequest;
import com.eldersphere.dtos.MedicalRecord.MedicalRecordResponse;
import com.eldersphere.entities.FileAsset;
import com.eldersphere.entities.MedicalRecord;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.services.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordDao medicalRecordDao;
    private final FileAssetDao fileAssetDao;

    @Override
    @Transactional
    public MedicalRecordResponse create(MedicalRecordRequest request) throws GenericException {
        MedicalRecord record = MedicalRecord.builder()
                .elderProfileId(request.getElderProfileId())
                .type(request.getType())
                .title(request.getTitle())
                .documentFileAssetId(request.getDocumentFileAssetId())
                .notes(request.getNotes())
                .sharedWithFamily(request.isSharedWithFamily())
                .build();
        return toResponse(medicalRecordDao.save(record));
    }

    @Override
    @Transactional
    public MedicalRecordResponse update(Long id, MedicalRecordRequest request) throws GenericException {
        MedicalRecord record = medicalRecordDao.findById(id, true);
        if (record == null) {
            throw new GenericException(ExceptionCodeEnum.MEDICAL_RECORD_NOT_FOUND, "Medical record not found");
        }
        record.setType(request.getType());
        record.setTitle(request.getTitle());
        record.setDocumentFileAssetId(request.getDocumentFileAssetId());
        record.setNotes(request.getNotes());
        record.setSharedWithFamily(request.isSharedWithFamily());
        return toResponse(medicalRecordDao.save(record));
    }

    @Override
    public MedicalRecordResponse getById(Long id) throws GenericException {
        MedicalRecord record = medicalRecordDao.findById(id, true);
        if (record == null) {
            throw new GenericException(ExceptionCodeEnum.MEDICAL_RECORD_NOT_FOUND, "Medical record not found");
        }
        return toResponse(record);
    }

    @Override
    public Page<MedicalRecordResponse> getByElderProfile(Long elderProfileId, boolean sharedOnly, Pageable pageable) {
        Page<MedicalRecord> page = sharedOnly
                ? medicalRecordDao.findSharedByElderProfileId(elderProfileId, pageable)
                : medicalRecordDao.findByElderProfileId(elderProfileId, pageable);
        return page.map(this::toResponse);
    }

    @Override
    @Transactional
    public void delete(Long id) throws GenericException {
        MedicalRecord record = medicalRecordDao.findById(id, true);
        if (record == null) {
            throw new GenericException(ExceptionCodeEnum.MEDICAL_RECORD_NOT_FOUND, "Medical record not found");
        }
        medicalRecordDao.deleteById(id);
    }

    private MedicalRecordResponse toResponse(MedicalRecord record) {
        MedicalRecordResponse response = new MedicalRecordResponse();
        response.setId(record.getId());
        response.setElderProfileId(record.getElderProfileId());
        response.setType(record.getType());
        response.setTitle(record.getTitle());
        response.setDocumentFileAssetId(record.getDocumentFileAssetId());
        if (record.getDocumentFileAssetId() != null) {
            FileAsset asset = fileAssetDao.findById(record.getDocumentFileAssetId(), true);
            if (asset != null) response.setDocumentUrl(asset.getUrl());
        }
        response.setNotes(record.getNotes());
        response.setSharedWithFamily(record.isSharedWithFamily());
        response.setCreatedBy(record.getCreatedBy());
        response.setCreatedAt(record.getCreatedAt());
        response.setUpdatedAt(record.getUpdatedAt());
        return response;
    }
}
