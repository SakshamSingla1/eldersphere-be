package com.eldersphere.repositories;

import com.eldersphere.entities.ElderProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElderProfileRepository extends JpaRepository<ElderProfile, Long> {
    List<ElderProfile> findByFamilyUserId(Long familyUserId);
    Page<ElderProfile> findByFamilyUserId(Long familyUserId, Pageable pageable);
    java.util.Optional<ElderProfile> findByElderUserId(Long elderUserId);

    // Profiles a family member either owns (family_user_id) or co-manages via an accepted
    // FamilyElderLink — see FamilyElderLink for why that's a separate join table rather than
    // a second scalar column on elder_profiles.
    @Query("""
            SELECT DISTINCT ep FROM ElderProfile ep
            LEFT JOIN FamilyElderLink link ON link.elderProfileId = ep.id
            WHERE ep.familyUserId = :familyUserId OR link.familyUserId = :familyUserId
            """)
    List<ElderProfile> findOwnedOrCoManagedByFamilyUserId(@Param("familyUserId") Long familyUserId);
}
