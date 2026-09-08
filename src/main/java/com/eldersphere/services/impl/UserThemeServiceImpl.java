package com.eldersphere.services.impl;

import com.eldersphere.dao.color_theme.ColorThemeDao;
import com.eldersphere.dao.user.UserDao;
import com.eldersphere.dtos.ColorTheme.UserThemeResponseDTO;
import com.eldersphere.entities.ColorTheme;
import com.eldersphere.entities.User;
import com.eldersphere.enums.ColorThemeStatusEnum;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.services.UserThemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserThemeServiceImpl implements UserThemeService {

    private final UserDao userDao;
    private final ColorThemeDao colorThemeDao;

    @Override
    public UserThemeResponseDTO getMyTheme(Long userId) throws GenericException {
        User user = userDao.findById(userId, true);
        if (user == null) {
            throw new GenericException(ExceptionCodeEnum.USER_NOT_FOUND, "User not found");
        }

        if (user.getActiveThemeId() != null) {
            ColorTheme picked = colorThemeDao.findById(user.getActiveThemeId(), true);
            if (picked != null && picked.getStatus() == ColorThemeStatusEnum.ACTIVE) {
                return toResponse(picked, false);
            }
            // Their pick was deleted or deactivated since - fall through to the default below
            // rather than erroring; the user simply sees the default until they pick again.
        }

        return resolveDefault();
    }

    @Override
    @Transactional
    public UserThemeResponseDTO setMyTheme(Long userId, Long themeId) throws GenericException {
        User user = userDao.findById(userId, true);
        if (user == null) {
            throw new GenericException(ExceptionCodeEnum.USER_NOT_FOUND, "User not found");
        }

        if (themeId == null) {
            user.setActiveThemeId(null);
            userDao.save(user);
            return resolveDefault();
        }

        ColorTheme theme = colorThemeDao.findById(themeId, true);
        if (theme == null) {
            throw new GenericException(ExceptionCodeEnum.COLOR_THEME_NOT_FOUND, "Color theme not found");
        }
        if (theme.getStatus() != ColorThemeStatusEnum.ACTIVE) {
            throw new GenericException(ExceptionCodeEnum.COLOR_THEME_INACTIVE, "Cannot select an inactive color theme");
        }

        user.setActiveThemeId(themeId);
        userDao.save(user);
        return toResponse(theme, false);
    }

    private UserThemeResponseDTO resolveDefault() {
        Optional<ColorTheme> defaultTheme = colorThemeDao.findByIsDefaultTrue()
                .filter(t -> t.getStatus() == ColorThemeStatusEnum.ACTIVE)
                .or(() -> colorThemeDao.findFirstByStatusOrderByIdAsc(ColorThemeStatusEnum.ACTIVE));

        return defaultTheme.map(theme -> toResponse(theme, true))
                // No ACTIVE theme exists at all (shouldn't happen given the seed migration) -
                // rather than throwing, hand back an explicit "nothing to show" response.
                .orElseGet(() -> UserThemeResponseDTO.builder().usingDefault(true).build());
    }

    private UserThemeResponseDTO toResponse(ColorTheme theme, boolean usingDefault) {
        return UserThemeResponseDTO.builder()
                .themeId(theme.getId())
                .themeName(theme.getName())
                .palette(theme.getPalette())
                .usingDefault(usingDefault)
                .build();
    }
}
