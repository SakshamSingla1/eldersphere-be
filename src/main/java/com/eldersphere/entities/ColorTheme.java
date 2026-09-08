package com.eldersphere.entities;

import com.eldersphere.audit.Auditable;
import com.eldersphere.converters.ColorPaletteConverter;
import com.eldersphere.enums.ColorThemeStatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "color_themes")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColorTheme extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Convert(converter = ColorPaletteConverter.class)
    @Column(columnDefinition = "TEXT", nullable = false)
    private ColorPalette palette;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Enumerated(EnumType.STRING)
    private ColorThemeStatusEnum status;
}
