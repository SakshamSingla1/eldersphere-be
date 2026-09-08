package com.eldersphere.services.impl;

import com.eldersphere.dao.color_theme.ColorThemeDao;
import com.eldersphere.dtos.ColorTheme.ColorThemeRequestDTO;
import com.eldersphere.dtos.ColorTheme.ColorThemeResponseDTO;
import com.eldersphere.entities.ColorTheme;
import com.eldersphere.enums.ColorThemeStatusEnum;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.services.ColorThemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ColorThemeServiceImpl implements ColorThemeService {

    private final ColorThemeDao colorThemeDao;

    @Override
    @Transactional
    public ColorThemeResponseDTO create(ColorThemeRequestDTO request) throws GenericException {
        if (colorThemeDao.existsByName(request.getName())) {
            throw new GenericException(ExceptionCodeEnum.DUPLICATE_COLOR_THEME, "A color theme with this name already exists");
        }
        if (request.isDefault()) {
            clearExistingDefault(null);
        }
        ColorTheme theme = ColorTheme.builder()
                .name(request.getName())
                .palette(request.getPalette())
                .isDefault(request.isDefault())
                .status(request.getStatus() != null ? request.getStatus() : ColorThemeStatusEnum.ACTIVE)
                .build();
        return toResponse(colorThemeDao.save(theme));
    }

    @Override
    @Transactional
    public ColorThemeResponseDTO update(Long id, ColorThemeRequestDTO request) throws GenericException {
        ColorTheme theme = colorThemeDao.findById(id, true);
        if (theme == null) {
            throw new GenericException(ExceptionCodeEnum.COLOR_THEME_NOT_FOUND, "Color theme not found");
        }
        if (colorThemeDao.existsByNameAndIdNot(request.getName(), id)) {
            throw new GenericException(ExceptionCodeEnum.DUPLICATE_COLOR_THEME, "A color theme with this name already exists");
        }
        if (request.isDefault() && !theme.isDefault()) {
            clearExistingDefault(id);
        }
        theme.setName(request.getName());
        theme.setPalette(request.getPalette());
        theme.setDefault(request.isDefault());
        if (request.getStatus() != null) theme.setStatus(request.getStatus());
        return toResponse(colorThemeDao.save(theme));
    }

    @Override
    public ColorThemeResponseDTO getById(Long id) throws GenericException {
        ColorTheme theme = colorThemeDao.findById(id, true);
        if (theme == null) {
            throw new GenericException(ExceptionCodeEnum.COLOR_THEME_NOT_FOUND, "Color theme not found");
        }
        return toResponse(theme);
    }

    @Override
    public List<ColorThemeResponseDTO> getAllActive() {
        return colorThemeDao.findByStatusOrderByIdAsc(ColorThemeStatusEnum.ACTIVE).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) throws GenericException {
        ColorTheme theme = colorThemeDao.findById(id, true);
        if (theme == null) {
            throw new GenericException(ExceptionCodeEnum.COLOR_THEME_NOT_FOUND, "Color theme not found");
        }
        if (theme.isDefault()) {
            throw new GenericException(ExceptionCodeEnum.CANNOT_DELETE_DEFAULT_COLOR_THEME,
                    "Cannot delete the default color theme - mark a different theme as default first");
        }
        // Any user whose active_theme_id pointed at this row falls back to the default theme
        // automatically (ON DELETE SET NULL on users.active_theme_id - see V40 migration).
        colorThemeDao.deleteById(id);
    }

    /** Unsets whichever theme (other than {@code exceptId}) currently holds is_default, so at
     * most one theme is ever flagged default. */
    private void clearExistingDefault(Long exceptId) {
        colorThemeDao.findByIsDefaultTrue()
                .filter(existingDefault -> exceptId == null || !existingDefault.getId().equals(exceptId))
                .ifPresent(existingDefault -> {
                    existingDefault.setDefault(false);
                    colorThemeDao.save(existingDefault);
                });
    }

    private ColorThemeResponseDTO toResponse(ColorTheme theme) {
        ColorThemeResponseDTO dto = new ColorThemeResponseDTO();
        dto.setId(theme.getId());
        dto.setName(theme.getName());
        dto.setPalette(theme.getPalette());
        dto.setDefault(theme.isDefault());
        dto.setStatus(theme.getStatus());
        dto.setCreatedAt(theme.getCreatedAt());
        dto.setUpdatedAt(theme.getUpdatedAt());
        return dto;
    }
}
