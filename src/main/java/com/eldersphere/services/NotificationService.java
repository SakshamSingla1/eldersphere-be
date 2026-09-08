package com.eldersphere.services;

import com.eldersphere.dtos.Notification.NotificationDeliveryLogResponse;
import com.eldersphere.dtos.Notification.NotificationResponseDTO;
import com.eldersphere.enums.NotificationTypeEnum;
import com.eldersphere.exceptions.GenericException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {

    void create(Long userId, NotificationTypeEnum type, String title, String message, String link);

    Page<NotificationResponseDTO> getByUser(Long userId, Pageable pageable);

    long getUnreadCount(Long userId);

    void markAsRead(Long id, Long userId) throws GenericException;

    void markAllAsRead(Long userId);

    void delete(Long id, Long userId) throws GenericException;

    List<NotificationDeliveryLogResponse> getDeliveryLogs(Long notificationId);
}
