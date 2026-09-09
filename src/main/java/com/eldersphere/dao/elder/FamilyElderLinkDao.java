package com.eldersphere.dao.elder;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.FamilyElderLink;
import com.eldersphere.repositories.FamilyElderLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FamilyElderLinkDao implements IDao<FamilyElderLink, Long> {

    private final FamilyElderLinkRepository familyElderLinkRepository;

    @Override
    public JpaRepository<FamilyElderLink, Long> getRepository() {
        return familyElderLinkRepository;
    }

    public FamilyElderLink save(FamilyElderLink link) {
        return familyElderLinkRepository.save(link);
    }

    public List<FamilyElderLink> findByElderProfileId(Long elderProfileId) {
        return familyElderLinkRepository.findByElderProfileId(elderProfileId);
    }

    public List<FamilyElderLink> findByFamilyUserId(Long familyUserId) {
        return familyElderLinkRepository.findByFamilyUserId(familyUserId);
    }

    public Optional<FamilyElderLink> findByElderProfileIdAndFamilyUserId(Long elderProfileId, Long familyUserId) {
        return familyElderLinkRepository.findByElderProfileIdAndFamilyUserId(elderProfileId, familyUserId);
    }

    public boolean existsByElderProfileIdAndFamilyUserId(Long elderProfileId, Long familyUserId) {
        return familyElderLinkRepository.existsByElderProfileIdAndFamilyUserId(elderProfileId, familyUserId);
    }

    public void deleteByElderProfileIdAndFamilyUserId(Long elderProfileId, Long familyUserId) {
        familyElderLinkRepository.deleteByElderProfileIdAndFamilyUserId(elderProfileId, familyUserId);
    }
}
