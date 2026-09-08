package com.eldersphere.repositories;

import com.eldersphere.entities.ServiceOffering;
import com.eldersphere.enums.ServiceCategoryEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceOfferingRepository extends JpaRepository<ServiceOffering, Long> {
    Page<ServiceOffering> findByCategory(ServiceCategoryEnum category, Pageable pageable);
}
