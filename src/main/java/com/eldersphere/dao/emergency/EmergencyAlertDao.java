package com.eldersphere.dao.emergency;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.EmergencyAlert;
import com.eldersphere.enums.EmergencyAlertStatusEnum;
import com.eldersphere.repositories.EmergencyAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EmergencyAlertDao implements IDao<EmergencyAlert, Long> {

    private final EmergencyAlertRepository emergencyAlertRepository;

    @Override
    public JpaRepository<EmergencyAlert, Long> getRepository() {
        return emergencyAlertRepository;
    }

    public EmergencyAlert save(EmergencyAlert alert) {
        return emergencyAlertRepository.save(alert);
    }

    public Page<EmergencyAlert> findByElderProfileId(Long elderProfileId, Pageable pageable) {
        return emergencyAlertRepository.findByElderProfileId(elderProfileId, pageable);
    }

    public Page<EmergencyAlert> findByStatus(EmergencyAlertStatusEnum status, Pageable pageable) {
        return emergencyAlertRepository.findByStatus(status, pageable);
    }

    public long countByStatus(EmergencyAlertStatusEnum status) {
        return emergencyAlertRepository.countByStatus(status);
    }

    public List<EmergencyAlert> findRecent() {
        return emergencyAlertRepository.findTop10ByOrderByTriggeredAtDesc();
    }

    public long countByElderProfileIdAndStatusIn(Long elderProfileId, List<EmergencyAlertStatusEnum> statuses) {
        return emergencyAlertRepository.countByElderProfileIdAndStatusIn(elderProfileId, statuses);
    }
}
