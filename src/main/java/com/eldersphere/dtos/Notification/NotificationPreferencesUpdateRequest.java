package com.eldersphere.dtos.Notification;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NotificationPreferencesUpdateRequest {

    @NotEmpty(message = "At least one preference entry is required")
    @Valid
    private List<NotificationPreferenceDTO> preferences;
}
