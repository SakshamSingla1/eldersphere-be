package com.eldersphere.dao.notification;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.Notification;
import com.eldersphere.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationDao implements IDao<Notification, Long> {

    private final NotificationRepository notificationRepository;

    @Override
    public JpaRepository<Notification, Long> getRepository() {
        return notificationRepository;
    }

    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

    public Page<Notification> findByUserId(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    public void deleteByUserIdAndId(Long userId, Long id) {
        notificationRepository.deleteByUserIdAndId(userId, id);
    }
}
