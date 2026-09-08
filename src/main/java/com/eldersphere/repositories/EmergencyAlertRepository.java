package com.eldersphere.repositories;

import com.eldersphere.entities.EmergencyAlert;
import com.eldersphere.enums.EmergencyAlertStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyAlertRepository extends JpaRepository<EmergencyAlert, Long> {
    Page<EmergencyAlert> findByElderProfileId(Long elderProfileId, Pageable pageable);
    Page<EmergencyAlert> findByStatus(EmergencyAlertStatusEnum status, Pageable pageable);
    long countByStatus(EmergencyAlertStatusEnum status);
    List<EmergencyAlert> findTop10ByOrderByTriggeredAtDesc();
    long countByElderProfileIdAndStatusIn(Long elderProfileId, List<EmergencyAlertStatusEnum> statuses);
}
