package com.eldersphere.services.impl;

import com.eldersphere.dao.color_theme.ColorThemeDao;
import com.eldersphere.dao.user.UserDao;
import com.eldersphere.dtos.ColorTheme.UserThemeResponseDTO;
import com.eldersphere.entities.BackgroundColors;
import com.eldersphere.entities.ColorGroup;
import com.eldersphere.entities.ColorPalette;
import com.eldersphere.entities.ColorTheme;
import com.eldersphere.entities.TextColors;
import com.eldersphere.entities.User;
import com.eldersphere.enums.ColorThemeStatusEnum;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.exceptions.GenericException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserThemeServiceImpl}: a user who never picked a theme (or whose pick
 * was since deleted/deactivated) must resolve to the current default theme rather than null or
 * an error, and setting/clearing a user's own active theme must validate the target id.
 */
class UserThemeServiceImplTest {

    private static final Long USER_ID = 42L;

    private UserDao userDao;
    private ColorThemeDao colorThemeDao;
    private UserThemeServiceImpl service;

    @BeforeEach
    void setUp() {
        userDao = mock(UserDao.class);
        colorThemeDao = mock(ColorThemeDao.class);
        service = new UserThemeServiceImpl(userDao, colorThemeDao);
    }

    @Test
    void getMyTheme_userNeverPickedOne_resolvesToDefault() throws GenericException {
        User user = User.builder().id(USER_ID).activeThemeId(null).build();
        ColorTheme defaultTheme = theme(1L, "Forest & Terracotta", true, ColorThemeStatusEnum.ACTIVE);
        when(userDao.findById(USER_ID, true)).thenReturn(user);
        when(colorThemeDao.findByIsDefaultTrue()).thenReturn(Optional.of(defaultTheme));

        UserThemeResponseDTO result = service.getMyTheme(USER_ID);

        assertThat(result.isUsingDefault()).isTrue();
        assertThat(result.getThemeId()).isEqualTo(1L);
        assertThat(result.getThemeName()).isEqualTo("Forest & Terracotta");
        assertThat(result.getPalette()).isNotNull();
    }

    @Test
    void getMyTheme_ownPickStillActive_returnsThatTheme() throws GenericException {
        User user = User.builder().id(USER_ID).activeThemeId(2L).build();
        ColorTheme picked = theme(2L, "Ocean Calm", false, ColorThemeStatusEnum.ACTIVE);
        when(userDao.findById(USER_ID, true)).thenReturn(user);
        when(colorThemeDao.findById(2L, true)).thenReturn(picked);

        UserThemeResponseDTO result = service.getMyTheme(USER_ID);

        assertThat(result.isUsingDefault()).isFalse();
        assertThat(result.getThemeName()).isEqualTo("Ocean Calm");
        verify(colorThemeDao, never()).findByIsDefaultTrue();
    }

    @Test
    void getMyTheme_ownPickWasDeactivated_fallsBackToDefault() throws GenericException {
        User user = User.builder().id(USER_ID).activeThemeId(2L).build();
        ColorTheme deactivatedPick = theme(2L, "Ocean Calm", false, ColorThemeStatusEnum.INACTIVE);
        ColorTheme defaultTheme = theme(1L, "Forest & Terracotta", true, ColorThemeStatusEnum.ACTIVE);
        when(userDao.findById(USER_ID, true)).thenReturn(user);
        when(colorThemeDao.findById(2L, true)).thenReturn(deactivatedPick);
        when(colorThemeDao.findByIsDefaultTrue()).thenReturn(Optional.of(defaultTheme));

        UserThemeResponseDTO result = service.getMyTheme(USER_ID);

        assertThat(result.isUsingDefault()).isTrue();
        assertThat(result.getThemeName()).isEqualTo("Forest & Terracotta");
    }

    @Test
    void getMyTheme_ownPickWasDeleted_fallsBackToDefault() throws GenericException {
        User user = User.builder().id(USER_ID).activeThemeId(999L).build();
        ColorTheme defaultTheme = theme(1L, "Forest & Terracotta", true, ColorThemeStatusEnum.ACTIVE);
        when(userDao.findById(USER_ID, true)).thenReturn(user);
        when(colorThemeDao.findById(999L, true)).thenReturn(null);
        when(colorThemeDao.findByIsDefaultTrue()).thenReturn(Optional.of(defaultTheme));

        UserThemeResponseDTO result = service.getMyTheme(USER_ID);

        assertThat(result.isUsingDefault()).isTrue();
        assertThat(result.getThemeName()).isEqualTo("Forest & Terracotta");
    }

    @Test
    void getMyTheme_noDefaultFlagged_fallsBackToFirstActiveTheme() throws GenericException {
        User user = User.builder().id(USER_ID).activeThemeId(null).build();
        ColorTheme firstActive = theme(3L, "Slate Professional", false, ColorThemeStatusEnum.ACTIVE);
        when(userDao.findById(USER_ID, true)).thenReturn(user);
        when(colorThemeDao.findByIsDefaultTrue()).thenReturn(Optional.empty());
        when(colorThemeDao.findFirstByStatusOrderByIdAsc(ColorThemeStatusEnum.ACTIVE)).thenReturn(Optional.of(firstActive));

        UserThemeResponseDTO result = service.getMyTheme(USER_ID);

        assertThat(result.isUsingDefault()).isTrue();
        assertThat(result.getThemeName()).isEqualTo("Slate Professional");
    }

    @Test
    void getMyTheme_userNotFound_throws() {
        when(userDao.findById(USER_ID, true)).thenReturn(null);

        assertThatThrownBy(() -> service.getMyTheme(USER_ID))
                .isInstanceOf(GenericException.class)
                .extracting(e -> ((GenericException) e).getExceptionCode())
                .isEqualTo(ExceptionCodeEnum.USER_NOT_FOUND);
    }

    @Test
    void setMyTheme_null_clearsSelectionAndFallsBackToDefault() throws GenericException {
        User user = User.builder().id(USER_ID).activeThemeId(2L).build();
        ColorTheme defaultTheme = theme(1L, "Forest & Terracotta", true, ColorThemeStatusEnum.ACTIVE);
        when(userDao.findById(USER_ID, true)).thenReturn(user);
        when(colorThemeDao.findByIsDefaultTrue()).thenReturn(Optional.of(defaultTheme));

        UserThemeResponseDTO result = service.setMyTheme(USER_ID, null);

        assertThat(user.getActiveThemeId()).isNull();
        assertThat(result.isUsingDefault()).isTrue();
        assertThat(result.getThemeName()).isEqualTo("Forest & Terracotta");
        verify(userDao).save(user);
    }

    @Test
    void setMyTheme_validActiveTheme_setsItOnTheUser() throws GenericException {
        User user = User.builder().id(USER_ID).activeThemeId(null).build();
        ColorTheme picked = theme(2L, "Ocean Calm", false, ColorThemeStatusEnum.ACTIVE);
        when(userDao.findById(USER_ID, true)).thenReturn(user);
        when(colorThemeDao.findById(2L, true)).thenReturn(picked);

        UserThemeResponseDTO result = service.setMyTheme(USER_ID, 2L);

        assertThat(user.getActiveThemeId()).isEqualTo(2L);
        assertThat(result.isUsingDefault()).isFalse();
        assertThat(result.getThemeName()).isEqualTo("Ocean Calm");
        verify(userDao).save(user);
    }

    @Test
    void setMyTheme_unknownThemeId_throwsNotFound() {
        User user = User.builder().id(USER_ID).build();
        when(userDao.findById(USER_ID, true)).thenReturn(user);
        when(colorThemeDao.findById(999L, true)).thenReturn(null);

        assertThatThrownBy(() -> service.setMyTheme(USER_ID, 999L))
                .isInstanceOf(GenericException.class)
                .extracting(e -> ((GenericException) e).getExceptionCode())
                .isEqualTo(ExceptionCodeEnum.COLOR_THEME_NOT_FOUND);
    }

    @Test
    void setMyTheme_inactiveThemeId_isRejected() {
        User user = User.builder().id(USER_ID).build();
        ColorTheme inactive = theme(5L, "Retired Theme", false, ColorThemeStatusEnum.INACTIVE);
        when(userDao.findById(USER_ID, true)).thenReturn(user);
        when(colorThemeDao.findById(5L, true)).thenReturn(inactive);

        assertThatThrownBy(() -> service.setMyTheme(USER_ID, 5L))
                .isInstanceOf(GenericException.class)
                .extracting(e -> ((GenericException) e).getExceptionCode())
                .isEqualTo(ExceptionCodeEnum.COLOR_THEME_INACTIVE);
    }

    private ColorTheme theme(Long id, String name, boolean isDefault, ColorThemeStatusEnum status) {
        return ColorTheme.builder().id(id).name(name).isDefault(isDefault).status(status).palette(samplePalette()).build();
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
