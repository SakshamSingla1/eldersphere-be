package com.eldersphere.dao.landing;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.LandingPageConfig;
import com.eldersphere.repositories.LandingPageConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LandingPageConfigDao implements IDao<LandingPageConfig, Long> {

    private final LandingPageConfigRepository landingPageConfigRepository;

    @Override
    public JpaRepository<LandingPageConfig, Long> getRepository() {
        return landingPageConfigRepository;
    }

    public LandingPageConfig save(LandingPageConfig config) {
        return landingPageConfigRepository.save(config);
    }

    public LandingPageConfig findFirst() {
        return landingPageConfigRepository.findAll().stream().findFirst().orElse(null);
    }
}
