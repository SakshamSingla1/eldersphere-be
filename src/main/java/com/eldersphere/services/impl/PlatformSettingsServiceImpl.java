package com.eldersphere.services.impl;

import com.eldersphere.dao.platform.PlatformSettingsDao;
import com.eldersphere.dtos.Platform.PlatformSettingsDTO;
import com.eldersphere.entities.PlatformSettings;
import com.eldersphere.services.PlatformSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlatformSettingsServiceImpl implements PlatformSettingsService {

    private static final Long SINGLETON_ID = 1L;

    private final PlatformSettingsDao platformSettingsDao;

    @Value("${app.emergency.default-sla-minutes}")
    private int defaultSlaMinutes;

    @Override
    public PlatformSettingsDTO getSettings() {
        PlatformSettings settings = platformSettingsDao.findById(SINGLETON_ID, true);
        if (settings == null) {
            return PlatformSettingsDTO.builder()
                    .platformName("ElderSphere")
                    .supportEmail("support@eldersphere.app")
                    .supportPhone(null)
                    .emergencyResponseSlaMinutes(defaultSlaMinutes)
                    .build();
        }
        return toResponse(settings);
    }

    @Override
    @Transactional
    public PlatformSettingsDTO updateSettings(PlatformSettingsDTO dto) {
        PlatformSettings settings = platformSettingsDao.findById(SINGLETON_ID, true);
        if (settings == null) {
            settings = new PlatformSettings();
            settings.setId(SINGLETON_ID);
        }
        settings.setPlatformName(dto.getPlatformName());
        settings.setSupportEmail(dto.getSupportEmail());
        settings.setSupportPhone(dto.getSupportPhone());
        settings.setEmergencyResponseSlaMinutes(dto.getEmergencyResponseSlaMinutes());
        return toResponse(platformSettingsDao.save(settings));
    }

    private PlatformSettingsDTO toResponse(PlatformSettings settings) {
        return PlatformSettingsDTO.builder()
                .platformName(settings.getPlatformName())
                .supportEmail(settings.getSupportEmail())
                .supportPhone(settings.getSupportPhone())
                .emergencyResponseSlaMinutes(settings.getEmergencyResponseSlaMinutes())
                .build();
    }
}
