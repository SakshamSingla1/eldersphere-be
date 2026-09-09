package com.eldersphere.dtos.ColorTheme;

import com.eldersphere.dtos.Common.AuditableResponse;
import com.eldersphere.entities.ColorPalette;
import com.eldersphere.enums.ColorThemeStatusEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ColorThemeResponseDTO extends AuditableResponse {
    private Long id;
    private String name;
    private ColorPalette palette;

    // Without @JsonProperty, Jackson would strip the "is" prefix from the Lombok-generated
    // isDefault()/setDefault() accessors and serialize this as the bare key "default" instead
    // of "isDefault" - explicit here so the wire contract is unambiguous for API consumers.
    @JsonProperty("isDefault")
    private boolean isDefault;

    private ColorThemeStatusEnum status;
}
