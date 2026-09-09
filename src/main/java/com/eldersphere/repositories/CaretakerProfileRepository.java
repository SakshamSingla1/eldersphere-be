package com.eldersphere.repositories;

import com.eldersphere.entities.CaretakerProfile;
import com.eldersphere.enums.CaretakerVerificationStatusEnum;
import com.eldersphere.enums.ServiceCategoryEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CaretakerProfileRepository extends JpaRepository<CaretakerProfile, Long> {

    Optional<CaretakerProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    long countByVerificationStatus(CaretakerVerificationStatusEnum status);

    @Query("""
            SELECT DISTINCT c FROM CaretakerProfile c
            LEFT JOIN c.specialties s
            JOIN User u ON u.id = c.userId
            WHERE (:category IS NULL OR s = :category)
            AND (:minRating IS NULL OR c.ratingAverage >= :minRating)
            AND (:verificationStatus IS NULL OR c.verificationStatus = :verificationStatus)
            AND (:minRate IS NULL OR c.hourlyRate >= :minRate)
            AND (:maxRate IS NULL OR c.hourlyRate <= :maxRate)
            AND (:locationPattern IS NULL OR LOWER(c.serviceArea) LIKE :locationPattern)
            AND (:queryPattern IS NULL
                OR LOWER(u.fullName) LIKE :queryPattern
                OR LOWER(u.email) LIKE :queryPattern
                OR u.phone LIKE :queryPattern)
            """)
    Page<CaretakerProfile> search(@Param("category") ServiceCategoryEnum category,
                                   @Param("minRating") Double minRating,
                                   @Param("verificationStatus") CaretakerVerificationStatusEnum verificationStatus,
                                   @Param("minRate") java.math.BigDecimal minRate,
                                   @Param("maxRate") java.math.BigDecimal maxRate,
                                   @Param("locationPattern") String locationPattern,
                                   @Param("queryPattern") String queryPattern,
                                   Pageable pageable);

    /**
     * Row shape: [caretaker_profile_id (Long), full_name (String), rating_average (Double),
     * completed_count (Long)]. No ORDER BY/LIMIT — the service sorts/truncates in Java
     * depending on which leaderboard metric was requested.
     */
    @Query(value = """
            SELECT cp.id AS caretaker_id, u.full_name AS full_name, cp.rating_average AS rating_average,
                   COALESCE(bc.completed_count, 0) AS completed_count
            FROM caretaker_profiles cp
            JOIN users u ON u.id = cp.user_id
            LEFT JOIN (
                SELECT caretaker_id, COUNT(*) AS completed_count
                FROM bookings WHERE status = 'COMPLETED'
                GROUP BY caretaker_id
            ) bc ON bc.caretaker_id = cp.id
            """, nativeQuery = true)
    java.util.List<Object[]> leaderboardRaw();
}
