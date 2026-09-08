package com.eldersphere.repositories;

import com.eldersphere.entities.LandingPageConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LandingPageConfigRepository extends JpaRepository<LandingPageConfig, Long> {
}
