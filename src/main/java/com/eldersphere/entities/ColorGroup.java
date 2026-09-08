package com.eldersphere.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One "main/light/dark/contrastText" color group - shaped to drop directly into a MUI
 * {@code PaletteColorOptions} (palette.primary, palette.secondary, palette.error,
 * palette.warning, palette.info, palette.success all take this exact shape).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColorGroup {
    private String main;
    private String light;
    private String dark;
    private String contrastText;
}
