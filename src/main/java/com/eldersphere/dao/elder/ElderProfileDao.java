package com.eldersphere.dao.elder;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.ElderProfile;
import com.eldersphere.repositories.ElderProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ElderProfileDao implements IDao<ElderProfile, Long> {

    private final ElderProfileRepository elderProfileRepository;

    @Override
    public JpaRepository<ElderProfile, Long> getRepository() {
        return elderProfileRepository;
    }

    public ElderProfile save(ElderProfile elderProfile) {
        return elderProfileRepository.save(elderProfile);
    }

    public List<ElderProfile> findByFamilyUserId(Long familyUserId) {
        return elderProfileRepository.findByFamilyUserId(familyUserId);
    }

    public List<ElderProfile> findOwnedOrCoManagedByFamilyUserId(Long familyUserId) {
        return elderProfileRepository.findOwnedOrCoManagedByFamilyUserId(familyUserId);
    }

    public java.util.Optional<ElderProfile> findByElderUserId(Long elderUserId) {
        return elderProfileRepository.findByElderUserId(elderUserId);
    }

    public Page<ElderProfile> findByFamilyUserId(Long familyUserId, Pageable pageable) {
        return elderProfileRepository.findByFamilyUserId(familyUserId, pageable);
    }

    public Page<ElderProfile> searchByName(String name, Pageable pageable) {
        return elderProfileRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    public void deleteById(Long id) {
        elderProfileRepository.deleteById(id);
    }
}
