package com.eldersphere.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The full palette a {@link ColorTheme} row stores as JSON (via {@link
 * com.eldersphere.converters.ColorPaletteConverter}). Deliberately flat and MUI-shaped rather
 * than the reference project's generic list-of-named-groups-of-50-900-shades structure: this
 * app has no per-user public branding page, only "which preset does the current user's own
 * dashboard use", so the frontend just needs to spread this object straight into {@code
 * createTheme({ palette: {...} })} without any reshaping. Every field here is named to match
 * its exact MUI counterpart:
 *
 * <pre>
 * {
 *   "primary":   { "main": "#2F6F5E", "light": "#4F8B7A", "dark": "#1D4E40", "contrastText": "#FFFFFF" },
 *   "secondary": { "main": "#E0875A", "light": "#EAA57F", "dark": "#B86A42", "contrastText": "#1B1B1B" },
 *   "error":     { "main": "#C62828", ... },
 *   "warning":   { "main": "#ED6C02", ... },
 *   "info":      { "main": "#01659C", ... },
 *   "success":   { "main": "#2E7D32", ... },
 *   "background": { "default": "#F7F4F0", "paper": "#FFFFFF" },
 *   "text":       { "primary": "#1E2A26", "secondary": "#55645F" }
 * }
 * </pre>
 *
 * {@code light}/{@code dark} on primary/secondary double as the values a dark-mode variant of
 * the same color theme would use (e.g. {@code primary.light} instead of {@code primary.main}
 * against a dark surface) - the existing light/dark mode toggle is a separate, orthogonal
 * frontend concern from which named preset is active, so this palette does not duplicate
 * background/text for both modes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColorPalette {
    private ColorGroup primary;
    private ColorGroup secondary;
    private ColorGroup error;
    private ColorGroup warning;
    private ColorGroup info;
    private ColorGroup success;
    private BackgroundColors background;
    private TextColors text;
}
