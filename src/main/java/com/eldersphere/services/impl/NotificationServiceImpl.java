package com.eldersphere.services.impl;

import com.eldersphere.dao.notification.NotificationDao;
import com.eldersphere.dao.notification.NotificationDeliveryLogDao;
import com.eldersphere.dao.notification.NotificationPreferenceDao;
import com.eldersphere.dao.user.UserDao;
import com.eldersphere.entities.NotificationPreference;
import com.eldersphere.dtos.Notification.NotificationDeliveryLogResponse;
import com.eldersphere.dtos.Notification.NotificationResponseDTO;
import com.eldersphere.entities.Notification;
import com.eldersphere.entities.NotificationDeliveryLog;
import com.eldersphere.entities.User;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.enums.NotificationChannelEnum;
import com.eldersphere.enums.NotificationTypeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.services.NotificationService;
import com.eldersphere.webpush.WebPushSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    // Notification types that additionally get simulated SMS/email "sends" logged, on top
    // of the always-created IN_APP delivery log. Real provider integration (Twilio/SendGrid)
    // is out of scope for this MVP — see NotificationChannelEnum.
    private static final Set<NotificationTypeEnum> MULTI_CHANNEL_TYPES =
            Set.of(NotificationTypeEnum.EMERGENCY_ALERT, NotificationTypeEnum.BOOKING_CONFIRMED,
                    NotificationTypeEnum.PAYMENT_RECEIVED, NotificationTypeEnum.PAYMENT_FAILED);

    // Defaults applied to a (user, type) combination with no NotificationPreference row yet -
    // see NotificationPreferenceServiceImpl, which these must stay in sync with.
    private static final boolean DEFAULT_IN_APP_ENABLED = true;
    private static final boolean DEFAULT_EMAIL_ENABLED = false;
    private static final boolean DEFAULT_SMS_ENABLED = false;
    private static final boolean DEFAULT_WEB_PUSH_ENABLED = false;

    private final NotificationDao notificationDao;
    private final NotificationDeliveryLogDao notificationDeliveryLogDao;
    private final NotificationPreferenceDao notificationPreferenceDao;
    private final UserDao userDao;
    private final SimpMessagingTemplate messagingTemplate;
    private final WebPushSenderService webPushSenderService;

    @Override
    @Transactional
    public void create(Long userId, NotificationTypeEnum type, String title, String message, String link) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .link(link)
                .isRead(false)
                .build();
        notification = notificationDao.save(notification);

        logDelivery(notification, userId);
        broadcast(userId, notification);
    }

    @Override
    public Page<NotificationResponseDTO> getByUser(Long userId, Pageable pageable) {
        return notificationDao.findByUserId(userId, pageable).map(this::toResponse);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationDao.countUnread(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long id, Long userId) throws GenericException {
        Notification notification = notificationDao.findById(id, true);
        if (notification == null || !notification.getUserId().equals(userId)) {
            throw new GenericException(ExceptionCodeEnum.NOTIFICATION_NOT_FOUND, "Notification not found");
        }
        notification.setRead(true);
        notificationDao.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 1000);
        notificationDao.findByUserId(userId, pageable).forEach(n -> {
            if (!n.isRead()) {
                n.setRead(true);
                notificationDao.save(n);
            }
        });
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) throws GenericException {
        Notification notification = notificationDao.findById(id, true);
        if (notification == null || !notification.getUserId().equals(userId)) {
            throw new GenericException(ExceptionCodeEnum.NOTIFICATION_NOT_FOUND, "Notification not found");
        }
        notificationDao.deleteByUserIdAndId(userId, id);
    }

    @Override
    public List<NotificationDeliveryLogResponse> getDeliveryLogs(Long notificationId) {
        return notificationDeliveryLogDao.findByNotificationId(notificationId).stream()
                .map(log -> NotificationDeliveryLogResponse.builder()
                        .id(log.getId())
                        .notificationId(log.getNotificationId())
                        .channel(log.getChannel())
                        .simulatedRecipient(log.getSimulatedRecipient())
                        .dispatchedAt(log.getDispatchedAt())
                        .build())
                .toList();
    }

    /**
     * Logs an IN_APP delivery, sends a real Web Push message (if the recipient has WEB_PUSH
     * enabled for this notification type and has any registered browser subscriptions — see
     * {@link WebPushSenderService}), and — for higher-urgency types (emergency alerts, booking
     * confirmations) — simulated SMS/email "sends" (a DB record plus an INFO log line only,
     * no real Twilio/SendGrid call; see NotificationChannelEnum). Each channel is only
     * actually attempted if the user's {@link NotificationPreference} for this notification
     * type (or the default, if they've never set one — see DEFAULT_* above) has that channel
     * enabled, so a user who has disabled a channel genuinely gets no delivery-log row for it.
     * Unlike SMS/email, WEB_PUSH is not restricted to {@link #MULTI_CHANNEL_TYPES}: getting
     * notified even when the app/tab is closed is the entire point of push, so it applies to
     * every notification type the recipient has opted into.
     */
    private void logDelivery(Notification notification, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        User user = userDao.findById(userId, true);
        NotificationPreference preference = notificationPreferenceDao
                .findByUserIdAndType(userId, notification.getType())
                .orElse(null);

        boolean inAppEnabled = preference != null ? preference.isInAppEnabled() : DEFAULT_IN_APP_ENABLED;
        boolean emailEnabled = preference != null ? preference.isEmailEnabled() : DEFAULT_EMAIL_ENABLED;
        boolean smsEnabled = preference != null ? preference.isSmsEnabled() : DEFAULT_SMS_ENABLED;
        boolean webPushEnabled = preference != null ? preference.isWebPushEnabled() : DEFAULT_WEB_PUSH_ENABLED;

        if (inAppEnabled) {
            notificationDeliveryLogDao.save(NotificationDeliveryLog.builder()
                    .notificationId(notification.getId())
                    .channel(NotificationChannelEnum.IN_APP)
                    .simulatedRecipient(user != null ? user.getEmail() : null)
                    .dispatchedAt(now)
                    .build());
        }

        if (webPushEnabled) {
            // Fails soft internally — a dead/expired subscription or push-service outage must
            // never throw back into this notification-creation flow. See WebPushSenderService.
            webPushSenderService.sendToUser(notification, userId);
        }

        if (!MULTI_CHANNEL_TYPES.contains(notification.getType())) {
            return;
        }

        String phone = user != null ? user.getPhone() : null;
        String email = user != null ? user.getEmail() : null;

        if (smsEnabled) {
            notificationDeliveryLogDao.save(NotificationDeliveryLog.builder()
                    .notificationId(notification.getId())
                    .channel(NotificationChannelEnum.SIMULATED_SMS)
                    .simulatedRecipient(phone)
                    .dispatchedAt(now)
                    .build());
            log.info("[SIMULATED SMS] to {} — {}: {}", phone, notification.getTitle(), notification.getMessage());
        }

        if (emailEnabled) {
            notificationDeliveryLogDao.save(NotificationDeliveryLog.builder()
                    .notificationId(notification.getId())
                    .channel(NotificationChannelEnum.SIMULATED_EMAIL)
                    .simulatedRecipient(email)
                    .dispatchedAt(now)
                    .build());
            log.info("[SIMULATED EMAIL] to {} — {}: {}", email, notification.getTitle(), notification.getMessage());
        }
    }

    private void broadcast(Long userId, Notification notification) {
        try {
            messagingTemplate.convertAndSend("/topic/notifications/" + userId, toResponse(notification));
        } catch (Exception e) {
            log.warn("Failed to broadcast notification {} to user {}: {}", notification.getId(), userId, e.getMessage());
        }
    }

    private NotificationResponseDTO toResponse(Notification notification) {
        return NotificationResponseDTO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .link(notification.getLink())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
