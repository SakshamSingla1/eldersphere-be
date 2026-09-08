package com.eldersphere.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Shaped to drop directly into MUI's {@code palette.background} ({@code default}/{@code paper}).
 * The Java field is {@code defaultBg} (since {@code default} is a reserved keyword) but is
 * serialized to/from the JSON key "default" via {@link JsonProperty}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackgroundColors {

    @JsonProperty("default")
    private String defaultBg;

    private String paper;
}
