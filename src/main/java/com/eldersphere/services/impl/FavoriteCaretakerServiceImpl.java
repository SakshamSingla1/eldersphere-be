package com.eldersphere.services.impl;

import com.eldersphere.dao.caretaker.CaretakerProfileDao;
import com.eldersphere.dao.caretaker.FavoriteCaretakerDao;
import com.eldersphere.dao.file.FileAssetDao;
import com.eldersphere.dao.user.UserDao;
import com.eldersphere.dtos.Search.CaretakerSearchResultDTO;
import com.eldersphere.entities.CaretakerProfile;
import com.eldersphere.entities.FavoriteCaretaker;
import com.eldersphere.entities.FileAsset;
import com.eldersphere.entities.User;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.services.FavoriteCaretakerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteCaretakerServiceImpl implements FavoriteCaretakerService {

    private final FavoriteCaretakerDao favoriteCaretakerDao;
    private final CaretakerProfileDao caretakerProfileDao;
    private final UserDao userDao;
    private final FileAssetDao fileAssetDao;

    @Override
    @Transactional
    public CaretakerSearchResultDTO addFavorite(Long familyUserId, Long caretakerId) throws GenericException {
        CaretakerProfile caretaker = caretakerProfileDao.findById(caretakerId, true);
        if (caretaker == null) {
            throw new GenericException(ExceptionCodeEnum.CARETAKER_PROFILE_NOT_FOUND, "Caretaker not found");
        }
        if (favoriteCaretakerDao.exists(familyUserId, caretakerId)) {
            throw new GenericException(ExceptionCodeEnum.DUPLICATE_FAVORITE_CARETAKER, "Caretaker is already in your favorites");
        }
        FavoriteCaretaker favorite = FavoriteCaretaker.builder()
                .familyUserId(familyUserId)
                .caretakerId(caretakerId)
                .build();
        favoriteCaretakerDao.save(favorite);
        return toResult(caretaker);
    }

    @Override
    @Transactional
    public void removeFavorite(Long familyUserId, Long caretakerId) throws GenericException {
        favoriteCaretakerDao.find(familyUserId, caretakerId)
                .orElseThrow(() -> new GenericException(ExceptionCodeEnum.FAVORITE_CARETAKER_NOT_FOUND, "This caretaker is not in your favorites"));
        favoriteCaretakerDao.delete(familyUserId, caretakerId);
    }

    @Override
    public List<CaretakerSearchResultDTO> listFavorites(Long familyUserId) {
        return favoriteCaretakerDao.findByFamilyUserId(familyUserId).stream()
                .map(FavoriteCaretaker::getCaretakerId)
                .map(id -> caretakerProfileDao.findById(id, true))
                .filter(java.util.Objects::nonNull)
                .map(this::toResult)
                .toList();
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
