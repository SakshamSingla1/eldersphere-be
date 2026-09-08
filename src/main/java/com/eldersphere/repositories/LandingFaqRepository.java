package com.eldersphere.repositories;

import com.eldersphere.entities.LandingFaq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LandingFaqRepository extends JpaRepository<LandingFaq, Long> {
    List<LandingFaq> findByIsActiveTrueOrderBySortOrderAsc();
}
