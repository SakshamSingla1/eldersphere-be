package com.eldersphere.services.impl;

import com.eldersphere.dao.caretaker.CaretakerProfileDao;
import com.eldersphere.dao.file.FileAssetDao;
import com.eldersphere.dao.user.UserDao;
import com.eldersphere.dtos.Search.CaretakerSearchResultDTO;
import com.eldersphere.entities.CaretakerProfile;
import com.eldersphere.entities.FileAsset;
import com.eldersphere.entities.User;
import com.eldersphere.enums.CaretakerVerificationStatusEnum;
import com.eldersphere.enums.ServiceCategoryEnum;
import com.eldersphere.services.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final CaretakerProfileDao caretakerProfileDao;
    private final UserDao userDao;
    private final FileAssetDao fileAssetDao;

    @Override
    public Page<CaretakerSearchResultDTO> searchCaretakers(ServiceCategoryEnum category, Double minRating,
                                                             CaretakerVerificationStatusEnum verificationStatus,
                                                             BigDecimal minRate, BigDecimal maxRate, String location,
                                                             Pageable pageable) {
        String normalizedLocation = (location != null && !location.isBlank()) ? location.trim() : null;
        Page<CaretakerProfile> page = caretakerProfileDao.search(category, minRating, verificationStatus,
                minRate, maxRate, normalizedLocation, pageable);
        return page.map(this::toResult);
    }

    private CaretakerSearchResultDTO toResult(CaretakerProfile profile) {
        User user = userDao.findById(profile.getUserId(), true);
        String profilePhotoUrl = null;
        if (profile.getProfilePhotoFileAssetId() != null) {
            FileAsset asset = fileAssetDao.findById(profile.getProfilePhotoFileAssetId(), true);
            if (asset != null) profilePhotoUrl = asset.getUrl();
        }
        return CaretakerSearchResultDTO.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .fullName(user != null ? user.getFullName() : null)
                .bio(profile.getBio())
                .specialties(profile.getSpecialties())
                .yearsOfExperience(profile.getYearsOfExperience())
                .hourlyRate(profile.getHourlyRate())
                .ratingAverage(profile.getRatingAverage())
                .verificationStatus(profile.getVerificationStatus())
                .profilePhotoUrl(profilePhotoUrl)
                .serviceArea(profile.getServiceArea())
                .build();
    }
}
