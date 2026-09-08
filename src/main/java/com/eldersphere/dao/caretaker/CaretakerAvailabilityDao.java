package com.eldersphere.dao.caretaker;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.CaretakerAvailability;
import com.eldersphere.repositories.CaretakerAvailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CaretakerAvailabilityDao implements IDao<CaretakerAvailability, Long> {

    private final CaretakerAvailabilityRepository caretakerAvailabilityRepository;

    @Override
    public JpaRepository<CaretakerAvailability, Long> getRepository() {
        return caretakerAvailabilityRepository;
    }

    public List<CaretakerAvailability> saveAll(List<CaretakerAvailability> slots) {
        return caretakerAvailabilityRepository.saveAll(slots);
    }

    public List<CaretakerAvailability> findByCaretakerId(Long caretakerId) {
        return caretakerAvailabilityRepository.findByCaretakerIdOrderByDayOfWeekAscStartTimeAsc(caretakerId);
    }

    @Transactional
    public void deleteByCaretakerId(Long caretakerId) {
        caretakerAvailabilityRepository.deleteByCaretakerId(caretakerId);
    }
}
