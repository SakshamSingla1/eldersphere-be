package com.eldersphere.dao.platform;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.PlatformSettings;
import com.eldersphere.repositories.PlatformSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PlatformSettingsDao implements IDao<PlatformSettings, Long> {

    private final PlatformSettingsRepository platformSettingsRepository;

    @Override
    public JpaRepository<PlatformSettings, Long> getRepository() {
        return platformSettingsRepository;
    }

    public PlatformSettings save(PlatformSettings settings) {
        return platformSettingsRepository.save(settings);
    }
}
