package com.eldersphere.dao.landing;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.LandingFeature;
import com.eldersphere.repositories.LandingFeatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class LandingFeatureDao implements IDao<LandingFeature, Long> {

    private final LandingFeatureRepository landingFeatureRepository;

    @Override
    public JpaRepository<LandingFeature, Long> getRepository() {
        return landingFeatureRepository;
    }

    public LandingFeature save(LandingFeature feature) {
        return landingFeatureRepository.save(feature);
    }

    public List<LandingFeature> findActive() {
        return landingFeatureRepository.findByIsActiveTrueOrderBySortOrderAsc();
    }

    public List<LandingFeature> findAll() {
        return landingFeatureRepository.findAll();
    }

    public void deleteById(Long id) {
        landingFeatureRepository.deleteById(id);
    }
}
