package com.eldersphere.services.impl;

import com.eldersphere.dao.caretaker.CaretakerProfileDao;
import com.eldersphere.dao.caretaker.CaretakerVerificationDocumentDao;
import com.eldersphere.dao.file.FileAssetDao;
import com.eldersphere.dao.user.UserDao;
import com.eldersphere.dtos.Caretaker.CaretakerProfileRequest;
import com.eldersphere.dtos.Caretaker.CaretakerProfileResponse;
import com.eldersphere.dtos.Caretaker.CaretakerVerificationDocumentResponse;
import com.eldersphere.dtos.Caretaker.CaretakerVerificationUpdateRequest;
import com.eldersphere.dtos.File.FileAssetResponse;
import com.eldersphere.entities.CaretakerProfile;
import com.eldersphere.entities.CaretakerVerificationDocument;
import com.eldersphere.entities.FileAsset;
import com.eldersphere.entities.User;
import com.eldersphere.enums.CaretakerVerificationStatusEnum;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.enums.ResourceTypeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.services.CaretakerProfileService;
import com.eldersphere.services.FileAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CaretakerProfileServiceImpl implements CaretakerProfileService {

    private final CaretakerProfileDao caretakerProfileDao;
    private final UserDao userDao;
    private final FileAssetDao fileAssetDao;
    private final FileAssetService fileAssetService;
    private final CaretakerVerificationDocumentDao caretakerVerificationDocumentDao;

    @Override
    @Transactional
    public CaretakerProfileResponse createOrUpdateForUser(Long userId, CaretakerProfileRequest request) throws GenericException {
        User user = userDao.findById(userId, true);
        if (user == null) {
            throw new GenericException(ExceptionCodeEnum.USER_NOT_FOUND, "User not found");
        }

        CaretakerProfile profile = caretakerProfileDao.findByUserId(userId).orElse(null);
        boolean isNew = profile == null;
        if (isNew) {
            profile = CaretakerProfile.builder()
                    .userId(userId)
                    .verificationStatus(CaretakerVerificationStatusEnum.PENDING)
                    .ratingAverage(0.0)
                    .build();
        }

        profile.setBio(request.getBio());
        profile.setSpecialties(request.getSpecialties() != null ? new HashSet<>(request.getSpecialties()) : new HashSet<>());
        profile.setYearsOfExperience(request.getYearsOfExperience());
        profile.setHourlyRate(request.getHourlyRate());
        profile.setServiceArea(request.getServiceArea());

        profile = caretakerProfileDao.save(profile);
        return toResponse(profile, user);
    }

    @Override
    public CaretakerProfileResponse getByUserId(Long userId) throws GenericException {
        CaretakerProfile profile = caretakerProfileDao.findByUserId(userId)
                .orElseThrow(() -> new GenericException(ExceptionCodeEnum.CARETAKER_PROFILE_NOT_FOUND, "Caretaker profile not found"));
        User user = userDao.findById(userId, true);
        return toResponse(profile, user);
    }

    @Override
    public CaretakerProfileResponse getById(Long id) throws GenericException {
        CaretakerProfile profile = caretakerProfileDao.findById(id, true);
        if (profile == null) {
            throw new GenericException(ExceptionCodeEnum.CARETAKER_PROFILE_NOT_FOUND, "Caretaker profile not found");
        }
        User user = userDao.findById(profile.getUserId(), true);
        return toResponse(profile, user);
    }

    @Override
    @Transactional
    public CaretakerProfileResponse updateVerification(Long id, CaretakerVerificationUpdateRequest request) throws GenericException {
        CaretakerProfile profile = caretakerProfileDao.findById(id, true);
        if (profile == null) {
            throw new GenericException(ExceptionCodeEnum.CARETAKER_PROFILE_NOT_FOUND, "Caretaker profile not found");
        }
        profile.setVerificationStatus(request.getVerificationStatus());
        profile = caretakerProfileDao.save(profile);
        User user = userDao.findById(profile.getUserId(), true);
        return toResponse(profile, user);
    }

    @Override
    @Transactional
    public CaretakerVerificationDocumentResponse uploadVerificationDocument(Long userId, MultipartFile file) throws GenericException {
        CaretakerProfile profile = caretakerProfileDao.findByUserId(userId)
                .orElseThrow(() -> new GenericException(ExceptionCodeEnum.CARETAKER_PROFILE_NOT_FOUND,
                        "Create your caretaker profile before submitting verification documents"));

        FileAssetResponse uploaded = fileAssetService.upload(file, ResourceTypeEnum.CARETAKER_VERIFICATION_DOCUMENT, userId);

        CaretakerVerificationDocument document = CaretakerVerificationDocument.builder()
                .caretakerProfileId(profile.getId())
                .fileAssetId(uploaded.getId())
                .uploadedBy(userId)
                .build();
        document = caretakerVerificationDocumentDao.save(document);
        return toDocumentResponse(document, uploaded);
    }

    @Override
    public List<CaretakerVerificationDocumentResponse> listVerificationDocuments(Long caretakerProfileId) throws GenericException {
        CaretakerProfile profile = caretakerProfileDao.findById(caretakerProfileId, true);
        if (profile == null) {
            throw new GenericException(ExceptionCodeEnum.CARETAKER_PROFILE_NOT_FOUND, "Caretaker profile not found");
        }
        return caretakerVerificationDocumentDao.findByCaretakerProfileId(caretakerProfileId).stream()
                .map(doc -> {
                    FileAsset asset = fileAssetDao.findById(doc.getFileAssetId(), true);
                    return toDocumentResponse(doc, asset);
                })
                .toList();
    }

    private CaretakerVerificationDocumentResponse toDocumentResponse(CaretakerVerificationDocument document, FileAssetResponse asset) {
        return CaretakerVerificationDocumentResponse.builder()
                .id(document.getId())
                .caretakerProfileId(document.getCaretakerProfileId())
                .fileAssetId(document.getFileAssetId())
                .fileName(asset != null ? asset.getFileName() : null)
                .fileType(asset != null ? asset.getFileType() : null)
                .url(asset != null ? asset.getUrl() : null)
                .uploadedBy(document.getUploadedBy())
                .createdAt(document.getCreatedAt())
                .build();
    }

    private CaretakerVerificationDocumentResponse toDocumentResponse(CaretakerVerificationDocument document, FileAsset asset) {
        return CaretakerVerificationDocumentResponse.builder()
                .id(document.getId())
                .caretakerProfileId(document.getCaretakerProfileId())
                .fileAssetId(document.getFileAssetId())
                .fileName(asset != null ? asset.getFileName() : null)
                .fileType(asset != null ? asset.getFileType() : null)
                .url(asset != null ? asset.getUrl() : null)
                .uploadedBy(document.getUploadedBy())
                .createdAt(document.getCreatedAt())
                .build();
    }

    private CaretakerProfileResponse toResponse(CaretakerProfile profile, User user) {
        CaretakerProfileResponse response = new CaretakerProfileResponse();
        response.setId(profile.getId());
        response.setUserId(profile.getUserId());
        if (user != null) {
            response.setFullName(user.getFullName());
            response.setEmail(user.getEmail());
            response.setPhone(user.getPhone());
        }
        response.setBio(profile.getBio());
        response.setSpecialties(profile.getSpecialties());
        response.setYearsOfExperience(profile.getYearsOfExperience());
        response.setHourlyRate(profile.getHourlyRate());
        response.setRatingAverage(profile.getRatingAverage());
        response.setVerificationStatus(profile.getVerificationStatus());
        response.setServiceArea(profile.getServiceArea());
        response.setProfilePhotoFileAssetId(profile.getProfilePhotoFileAssetId());
        if (profile.getProfilePhotoFileAssetId() != null) {
            FileAsset asset = fileAssetDao.findById(profile.getProfilePhotoFileAssetId(), true);
            if (asset != null) response.setProfilePhotoUrl(asset.getUrl());
        }
        response.setCreatedAt(profile.getCreatedAt());
        response.setUpdatedAt(profile.getUpdatedAt());
        return response;
    }
}
