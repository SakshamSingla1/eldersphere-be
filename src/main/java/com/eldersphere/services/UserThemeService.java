package com.eldersphere.services;

import com.eldersphere.dtos.ColorTheme.UserThemeResponseDTO;
import com.eldersphere.exceptions.GenericException;

public interface UserThemeService {

    /**
     * Resolves the caller's active color theme: their own explicit pick if it still exists and
     * is ACTIVE, otherwise the current default theme (or, failing that, the first ACTIVE theme),
     * with {@code usingDefault} telling the caller which case it was.
     */
    UserThemeResponseDTO getMyTheme(Long userId) throws GenericException;

    /** {@code themeId} null resets the caller to the default theme. Rejects an unknown or
     * INACTIVE theme id. */
    UserThemeResponseDTO setMyTheme(Long userId, Long themeId) throws GenericException;
}
