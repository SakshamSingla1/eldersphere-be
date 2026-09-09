package com.eldersphere.converters;

import com.eldersphere.entities.BackgroundColors;
import com.eldersphere.entities.ColorGroup;
import com.eldersphere.entities.ColorPalette;
import com.eldersphere.entities.TextColors;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The palette JSON converter must round-trip a {@link ColorPalette} through its TEXT column
 * representation without losing any field - including the "default" background key, which is
 * a reserved Java keyword and so is remapped via {@code @JsonProperty} rather than matching the
 * Java field name directly.
 */
class ColorPaletteConverterTest {

    private final ColorPaletteConverter converter = new ColorPaletteConverter();

    @Test
    void roundTrip_preservesEveryField() {
        ColorPalette original = ColorPalette.builder()
                .primary(ColorGroup.builder().main("#2F6F5E").light("#4F8B7A").dark("#1D4E40").contrastText("#FFFFFF").build())
                .secondary(ColorGroup.builder().main("#E0875A").light("#EAA57F").dark("#B86A42").contrastText("#1B1B1B").build())
                .error(ColorGroup.builder().main("#C62828").light("#E57373").dark("#8E0000").contrastText("#FFFFFF").build())
                .warning(ColorGroup.builder().main("#ED6C02").light("#FFB74D").dark("#B53D00").contrastText("#000000").build())
                .info(ColorGroup.builder().main("#01659C").light("#4FA8D8").dark("#013E61").contrastText("#FFFFFF").build())
                .success(ColorGroup.builder().main("#2E7D32").light("#66BB6A").dark("#1B5E20").contrastText("#FFFFFF").build())
                .background(BackgroundColors.builder().defaultBg("#F7F4F0").paper("#FFFFFF").build())
                .text(TextColors.builder().primary("#1E2A26").secondary("#55645F").build())
                .build();

        String json = converter.convertToDatabaseColumn(original);
        assertThat(json).isNotNull();
        // The Java field is "defaultBg" (can't be named the reserved word "default"), but the
        // wire/DB JSON key must still literally be "default" for the frontend to spread this
        // straight into MUI's palette.background.
        assertThat(json).contains("\"background\"").contains("\"default\":\"#F7F4F0\"").doesNotContain("defaultBg");

        ColorPalette roundTripped = converter.convertToEntityAttribute(json);

        assertThat(roundTripped).isEqualTo(original);
        assertThat(roundTripped.getPrimary().getMain()).isEqualTo("#2F6F5E");
        assertThat(roundTripped.getBackground().getDefaultBg()).isEqualTo("#F7F4F0");
        assertThat(roundTripped.getBackground().getPaper()).isEqualTo("#FFFFFF");
        assertThat(roundTripped.getText().getSecondary()).isEqualTo("#55645F");
    }

    @Test
    void convertToDatabaseColumn_null_returnsNull() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    void convertToEntityAttribute_nullOrBlank_returnsNull() {
        assertThat(converter.convertToEntityAttribute(null)).isNull();
        assertThat(converter.convertToEntityAttribute("")).isNull();
        assertThat(converter.convertToEntityAttribute("   ")).isNull();
    }

    @Test
    void convertToEntityAttribute_malformedJson_returnsNullRatherThanThrowing() {
        assertThat(converter.convertToEntityAttribute("{not valid json")).isNull();
    }
}
