package com.eldersphere.dao.color_theme;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.ColorTheme;
import com.eldersphere.enums.ColorThemeStatusEnum;
import com.eldersphere.repositories.ColorThemeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ColorThemeDao implements IDao<ColorTheme, Long> {

    private final ColorThemeRepository colorThemeRepository;

    @Override
    public JpaRepository<ColorTheme, Long> getRepository() {
        return colorThemeRepository;
    }

    public ColorTheme save(ColorTheme theme) {
        return colorThemeRepository.save(theme);
    }

    public boolean existsByName(String name) {
        return colorThemeRepository.existsByName(name);
    }

    public boolean existsByNameAndIdNot(String name, Long id) {
        return colorThemeRepository.existsByNameAndIdNot(name, id);
    }

    public List<ColorTheme> findByStatusOrderByIdAsc(ColorThemeStatusEnum status) {
        return colorThemeRepository.findByStatusOrderByIdAsc(status);
    }

    public Optional<ColorTheme> findByIsDefaultTrue() {
        return colorThemeRepository.findByIsDefaultTrue();
    }

    public Optional<ColorTheme> findFirstByStatusOrderByIdAsc(ColorThemeStatusEnum status) {
        return colorThemeRepository.findFirstByStatusOrderByIdAsc(status);
    }

    public void deleteById(Long id) {
        colorThemeRepository.deleteById(id);
    }
}
