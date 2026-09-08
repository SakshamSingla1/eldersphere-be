package com.eldersphere.repositories;

import com.eldersphere.entities.ColorTheme;
import com.eldersphere.enums.ColorThemeStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ColorThemeRepository extends JpaRepository<ColorTheme, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    List<ColorTheme> findByStatusOrderByIdAsc(ColorThemeStatusEnum status);

    Optional<ColorTheme> findByIsDefaultTrue();

    Optional<ColorTheme> findFirstByStatusOrderByIdAsc(ColorThemeStatusEnum status);
}
