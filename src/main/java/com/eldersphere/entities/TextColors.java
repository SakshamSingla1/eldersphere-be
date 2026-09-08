package com.eldersphere.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Shaped to drop directly into MUI's {@code palette.text} ({@code primary}/{@code secondary}). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextColors {
    private String primary;
    private String secondary;
}
