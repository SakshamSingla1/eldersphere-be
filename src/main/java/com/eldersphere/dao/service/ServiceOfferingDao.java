package com.eldersphere.dao.service;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.ServiceOffering;
import com.eldersphere.enums.ServiceCategoryEnum;
import com.eldersphere.repositories.ServiceOfferingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ServiceOfferingDao implements IDao<ServiceOffering, Long> {

    private final ServiceOfferingRepository serviceOfferingRepository;

    @Override
    public JpaRepository<ServiceOffering, Long> getRepository() {
        return serviceOfferingRepository;
    }

    public ServiceOffering save(ServiceOffering serviceOffering) {
        return serviceOfferingRepository.save(serviceOffering);
    }

    public Page<ServiceOffering> findByCategory(ServiceCategoryEnum category, Pageable pageable) {
        return serviceOfferingRepository.findByCategory(category, pageable);
    }

    public void deleteById(Long id) {
        serviceOfferingRepository.deleteById(id);
    }
}
