package com.eldersphere.services.impl;

import com.eldersphere.dao.caretaker.CaretakerProfileDao;
import com.eldersphere.dao.file.FileAssetDao;
import com.eldersphere.dao.user.UserDao;
import com.eldersphere.dtos.Search.CaretakerSearchResultDTO;
import com.eldersphere.dtos.User.UserLinkSearchResultDTO;
import com.eldersphere.entities.CaretakerProfile;
import com.eldersphere.entities.FileAsset;
import com.eldersphere.entities.User;
import com.eldersphere.enums.CaretakerVerificationStatusEnum;
import com.eldersphere.enums.ServiceCategoryEnum;
import com.eldersphere.enums.UserTypeEnum;
import com.eldersphere.services.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private static final Set<UserTypeEnum> LINKABLE_TYPES = EnumSet.of(UserTypeEnum.ELDER, UserTypeEnum.FAMILY_MEMBER);
    private static final int LINK_SEARCH_LIMIT = 10;

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

    @Override
    public List<UserLinkSearchResultDTO> searchLinkableUsers(String query, UserTypeEnum userType, User caller) {
        if (query == null || query.trim().length() < 2) {
            return List.of();
        }
        List<UserTypeEnum> types = (userType != null && LINKABLE_TYPES.contains(userType))
                ? List.of(userType)
                : List.copyOf(LINKABLE_TYPES);
        return userDao.searchLinkable(query.trim(), caller.getId(), types, PageRequest.of(0, LINK_SEARCH_LIMIT)).stream()
                .map(this::toLinkSearchResult)
                .toList();
    }

    private UserLinkSearchResultDTO toLinkSearchResult(User user) {
        return UserLinkSearchResultDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .maskedEmail(maskEmail(user.getEmail()))
                .maskedPhone(maskPhone(user.getPhone()))
                .userType(user.getUserType())
                .build();
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@", 2);
        String local = parts[0];
        String masked = local.length() <= 1 ? local : local.charAt(0) + "***";
        return masked + "@" + parts[1];
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return phone;
        return "***-***-" + phone.substring(phone.length() - 4);
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
