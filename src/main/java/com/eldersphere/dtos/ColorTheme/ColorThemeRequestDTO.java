package com.eldersphere.dtos.ColorTheme;

import com.eldersphere.entities.ColorPalette;
import com.eldersphere.enums.ColorThemeStatusEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ColorThemeRequestDTO {

    @NotBlank(message = "Theme name is required")
    private String name;

    @NotNull(message = "Palette is required")
    @Valid
    private ColorPalette palette;

    /** When true, this theme becomes THE default and any previously-default theme is unset. */
    private boolean isDefault;

    private ColorThemeStatusEnum status;
}
