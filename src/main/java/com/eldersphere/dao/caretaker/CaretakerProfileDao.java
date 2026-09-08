package com.eldersphere.dao.caretaker;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.CaretakerProfile;
import com.eldersphere.enums.CaretakerVerificationStatusEnum;
import com.eldersphere.enums.ServiceCategoryEnum;
import com.eldersphere.repositories.CaretakerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CaretakerProfileDao implements IDao<CaretakerProfile, Long> {

    private final CaretakerProfileRepository caretakerProfileRepository;

    @Override
    public JpaRepository<CaretakerProfile, Long> getRepository() {
        return caretakerProfileRepository;
    }

    public CaretakerProfile save(CaretakerProfile profile) {
        return caretakerProfileRepository.save(profile);
    }

    public Optional<CaretakerProfile> findByUserId(Long userId) {
        return caretakerProfileRepository.findByUserId(userId);
    }

    public boolean existsByUserId(Long userId) {
        return caretakerProfileRepository.existsByUserId(userId);
    }

    public long countByVerificationStatus(CaretakerVerificationStatusEnum status) {
        return caretakerProfileRepository.countByVerificationStatus(status);
    }

    public Page<CaretakerProfile> search(ServiceCategoryEnum category, Double minRating,
                                          CaretakerVerificationStatusEnum verificationStatus,
                                          java.math.BigDecimal minRate, java.math.BigDecimal maxRate,
                                          String location, Pageable pageable) {
        String locationPattern = (location != null && !location.isBlank())
                ? "%" + location.toLowerCase() + "%" : null;
        return caretakerProfileRepository.search(category, minRating, verificationStatus, minRate, maxRate, locationPattern, pageable);
    }

    public java.util.List<Object[]> leaderboardRaw() {
        return caretakerProfileRepository.leaderboardRaw();
    }
}
