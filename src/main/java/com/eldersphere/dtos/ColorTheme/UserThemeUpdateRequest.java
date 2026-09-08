package com.eldersphere.dtos.ColorTheme;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** {@code themeId} null resets the caller back to the default theme. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserThemeUpdateRequest {
    private Long themeId;
}
