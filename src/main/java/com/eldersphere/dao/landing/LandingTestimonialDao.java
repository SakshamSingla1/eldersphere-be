package com.eldersphere.dao.landing;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.LandingTestimonial;
import com.eldersphere.repositories.LandingTestimonialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class LandingTestimonialDao implements IDao<LandingTestimonial, Long> {

    private final LandingTestimonialRepository landingTestimonialRepository;

    @Override
    public JpaRepository<LandingTestimonial, Long> getRepository() {
        return landingTestimonialRepository;
    }

    public LandingTestimonial save(LandingTestimonial testimonial) {
        return landingTestimonialRepository.save(testimonial);
    }

    public List<LandingTestimonial> findActive() {
        return landingTestimonialRepository.findByIsActiveTrueOrderBySortOrderAsc();
    }

    public List<LandingTestimonial> findAll() {
        return landingTestimonialRepository.findAll();
    }

    public void deleteById(Long id) {
        landingTestimonialRepository.deleteById(id);
    }
}
