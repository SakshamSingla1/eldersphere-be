package com.eldersphere.converters;

import com.eldersphere.entities.ColorPalette;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Mirrors the reference portfolio-be {@code ColorPaletteConverter} pattern: a {@link ColorPalette}
 * is stored as a single JSON blob in a TEXT column rather than being normalized into rows. */
@Converter
public class ColorPaletteConverter implements AttributeConverter<ColorPalette, String> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(ColorPalette attribute) {
        if (attribute == null) return null;
        try {
            return mapper.writeValueAsString(attribute);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public ColorPalette convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return null;
        try {
            return mapper.readValue(dbData, ColorPalette.class);
        } catch (Exception e) {
            return null;
        }
    }
}
