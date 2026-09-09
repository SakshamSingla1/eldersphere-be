package com.eldersphere.services.impl;

import com.eldersphere.dao.color_theme.ColorThemeDao;
import com.eldersphere.dtos.ColorTheme.ColorThemeRequestDTO;
import com.eldersphere.dtos.ColorTheme.ColorThemeResponseDTO;
import com.eldersphere.entities.BackgroundColors;
import com.eldersphere.entities.ColorGroup;
import com.eldersphere.entities.ColorPalette;
import com.eldersphere.entities.ColorTheme;
import com.eldersphere.entities.TextColors;
import com.eldersphere.enums.ColorThemeStatusEnum;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.exceptions.GenericException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ColorThemeServiceImpl}: the SUPER_ADMIN-only CRUD business rules that
 * don't need a real database - duplicate name rejection, "at most one default theme" enforcement
 * (setting a new default un-sets whichever theme previously held it), and refusing to delete the
 * current default theme.
 */
class ColorThemeServiceImplTest {

    private ColorThemeDao colorThemeDao;
    private ColorThemeServiceImpl service;

    @BeforeEach
    void setUp() {
        colorThemeDao = mock(ColorThemeDao.class);
        service = new ColorThemeServiceImpl(colorThemeDao);
        when(colorThemeDao.save(any(ColorTheme.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void create_duplicateName_throwsConflict() {
        when(colorThemeDao.existsByName("Ocean Calm")).thenReturn(true);

        ColorThemeRequestDTO request = requestFor("Ocean Calm", false);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(GenericException.class)
                .extracting(e -> ((GenericException) e).getExceptionCode())
                .isEqualTo(ExceptionCodeEnum.DUPLICATE_COLOR_THEME);
    }

    @Test
    void create_asNewDefault_unsetsThePreviousDefaultTheme() throws GenericException {
        ColorTheme previousDefault = ColorTheme.builder().id(1L).name("Forest & Terracotta").isDefault(true)
                .status(ColorThemeStatusEnum.ACTIVE).palette(samplePalette()).build();
        when(colorThemeDao.existsByName(any())).thenReturn(false);
        when(colorThemeDao.findByIsDefaultTrue()).thenReturn(Optional.of(previousDefault));

        ColorThemeResponseDTO created = service.create(requestFor("New Default Theme", true));

        assertThat(created.isDefault()).isTrue();
        assertThat(previousDefault.isDefault()).isFalse();
        verify(colorThemeDao).save(previousDefault);
    }

    @Test
    void create_notDefault_doesNotTouchExistingDefault() throws GenericException {
        when(colorThemeDao.existsByName(any())).thenReturn(false);

        service.create(requestFor("Non Default Theme", false));

        verify(colorThemeDao, never()).findByIsDefaultTrue();
    }

    @Test
    void update_unknownId_throwsNotFound() {
        when(colorThemeDao.findById(99L, true)).thenReturn(null);

        assertThatThrownBy(() -> service.update(99L, requestFor("Whatever", false)))
                .isInstanceOf(GenericException.class)
                .extracting(e -> ((GenericException) e).getExceptionCode())
                .isEqualTo(ExceptionCodeEnum.COLOR_THEME_NOT_FOUND);
    }

    @Test
    void delete_currentDefaultTheme_isRejected() {
        ColorTheme defaultTheme = ColorTheme.builder().id(1L).name("Forest & Terracotta").isDefault(true)
                .status(ColorThemeStatusEnum.ACTIVE).palette(samplePalette()).build();
        when(colorThemeDao.findById(1L, true)).thenReturn(defaultTheme);

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(GenericException.class)
                .extracting(e -> ((GenericException) e).getExceptionCode())
                .isEqualTo(ExceptionCodeEnum.CANNOT_DELETE_DEFAULT_COLOR_THEME);

        verify(colorThemeDao, never()).deleteById(anyLong());
    }

    @Test
    void delete_nonDefaultTheme_deletesIt() throws GenericException {
        ColorTheme theme = ColorTheme.builder().id(2L).name("Ocean Calm").isDefault(false)
                .status(ColorThemeStatusEnum.ACTIVE).palette(samplePalette()).build();
        when(colorThemeDao.findById(2L, true)).thenReturn(theme);

        service.delete(2L);

        verify(colorThemeDao).deleteById(2L);
    }

    @Test
    void getAllActive_onlyReturnsActiveThemes() {
        ColorTheme active = ColorTheme.builder().id(1L).name("Active One").status(ColorThemeStatusEnum.ACTIVE).palette(samplePalette()).build();
        when(colorThemeDao.findByStatusOrderByIdAsc(ColorThemeStatusEnum.ACTIVE)).thenReturn(List.of(active));

        List<ColorThemeResponseDTO> result = service.getAllActive();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Active One");
    }

    private ColorThemeRequestDTO requestFor(String name, boolean isDefault) {
        return ColorThemeRequestDTO.builder()
                .name(name)
                .palette(samplePalette())
                .isDefault(isDefault)
                .status(ColorThemeStatusEnum.ACTIVE)
                .build();
    }

    private ColorPalette samplePalette() {
        return ColorPalette.builder()
                .primary(ColorGroup.builder().main("#123456").light("#345678").dark("#012345").contrastText("#FFFFFF").build())
                .secondary(ColorGroup.builder().main("#654321").light("#765432").dark("#543210").contrastText("#FFFFFF").build())
                .background(BackgroundColors.builder().defaultBg("#F0F0F0").paper("#FFFFFF").build())
                .text(TextColors.builder().primary("#111111").secondary("#444444").build())
                .build();
    }
}
