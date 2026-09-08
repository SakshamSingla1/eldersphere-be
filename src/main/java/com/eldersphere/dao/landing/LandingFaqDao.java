package com.eldersphere.dao.landing;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.LandingFaq;
import com.eldersphere.repositories.LandingFaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class LandingFaqDao implements IDao<LandingFaq, Long> {

    private final LandingFaqRepository landingFaqRepository;

    @Override
    public JpaRepository<LandingFaq, Long> getRepository() {
        return landingFaqRepository;
    }

    public LandingFaq save(LandingFaq faq) {
        return landingFaqRepository.save(faq);
    }

    public List<LandingFaq> findActive() {
        return landingFaqRepository.findByIsActiveTrueOrderBySortOrderAsc();
    }

    public List<LandingFaq> findAll() {
        return landingFaqRepository.findAll();
    }

    public void deleteById(Long id) {
        landingFaqRepository.deleteById(id);
    }
}
