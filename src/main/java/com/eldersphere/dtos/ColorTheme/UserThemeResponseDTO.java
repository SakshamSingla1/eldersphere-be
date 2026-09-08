package com.eldersphere.dtos.ColorTheme;

import com.eldersphere.entities.ColorPalette;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The caller's resolved active theme. Always carries a concrete {@code palette} - if the user
 * has never picked one (or their pick was deleted/deactivated), this resolves to the current
 * default theme rather than coming back null, so the frontend never has to special-case "no
 * theme" when rendering. {@code usingDefault} tells the frontend whether this is the user's own
 * explicit choice or the fallback, purely informational (e.g. to show "(default)" in a picker).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserThemeResponseDTO {
    private Long themeId;
    private String themeName;
    private ColorPalette palette;
    private boolean usingDefault;
}
