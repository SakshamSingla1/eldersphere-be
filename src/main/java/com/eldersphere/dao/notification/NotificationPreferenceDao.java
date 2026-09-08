package com.eldersphere.dao.notification;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.NotificationPreference;
import com.eldersphere.enums.NotificationTypeEnum;
import com.eldersphere.repositories.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationPreferenceDao implements IDao<NotificationPreference, Long> {

    private final NotificationPreferenceRepository notificationPreferenceRepository;

    @Override
    public JpaRepository<NotificationPreference, Long> getRepository() {
        return notificationPreferenceRepository;
    }

    public NotificationPreference save(NotificationPreference preference) {
        return notificationPreferenceRepository.save(preference);
    }

    public List<NotificationPreference> findByUserId(Long userId) {
        return notificationPreferenceRepository.findByUserId(userId);
    }

    public Optional<NotificationPreference> findByUserIdAndType(Long userId, NotificationTypeEnum type) {
        return notificationPreferenceRepository.findByUserIdAndNotificationType(userId, type);
    }
}
