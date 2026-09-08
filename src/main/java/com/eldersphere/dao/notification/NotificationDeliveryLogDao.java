package com.eldersphere.dao.notification;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.NotificationDeliveryLog;
import com.eldersphere.repositories.NotificationDeliveryLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class NotificationDeliveryLogDao implements IDao<NotificationDeliveryLog, Long> {

    private final NotificationDeliveryLogRepository notificationDeliveryLogRepository;

    @Override
    public JpaRepository<NotificationDeliveryLog, Long> getRepository() {
        return notificationDeliveryLogRepository;
    }

    public NotificationDeliveryLog save(NotificationDeliveryLog log) {
        return notificationDeliveryLogRepository.save(log);
    }

    public List<NotificationDeliveryLog> findByNotificationId(Long notificationId) {
        return notificationDeliveryLogRepository.findByNotificationId(notificationId);
    }
}
