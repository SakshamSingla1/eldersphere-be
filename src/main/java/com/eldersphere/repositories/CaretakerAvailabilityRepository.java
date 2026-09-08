package com.eldersphere.repositories;

import com.eldersphere.entities.CaretakerAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaretakerAvailabilityRepository extends JpaRepository<CaretakerAvailability, Long> {

    List<CaretakerAvailability> findByCaretakerIdOrderByDayOfWeekAscStartTimeAsc(Long caretakerId);

    void deleteByCaretakerId(Long caretakerId);
}
