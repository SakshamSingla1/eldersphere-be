package com.eldersphere.dtos.ColorTheme;

import com.eldersphere.dtos.Common.AuditableResponse;
import com.eldersphere.entities.ColorPalette;
import com.eldersphere.enums.ColorThemeStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ColorThemeResponseDTO extends AuditableResponse {
    private Long id;
    private String name;
    private ColorPalette palette;
    private boolean isDefault;
    private ColorThemeStatusEnum status;
}
