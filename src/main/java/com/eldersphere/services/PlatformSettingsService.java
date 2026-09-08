package com.eldersphere.services;

import com.eldersphere.dtos.Platform.PlatformSettingsDTO;

public interface PlatformSettingsService {
    PlatformSettingsDTO getSettings();
    PlatformSettingsDTO updateSettings(PlatformSettingsDTO dto);
}
