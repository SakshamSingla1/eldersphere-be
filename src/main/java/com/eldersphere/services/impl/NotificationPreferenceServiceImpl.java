package com.eldersphere.services.impl;

import com.eldersphere.dao.notification.NotificationPreferenceDao;
import com.eldersphere.dtos.Notification.NotificationPreferenceDTO;
import com.eldersphere.entities.NotificationPreference;
import com.eldersphere.enums.NotificationTypeEnum;
import com.eldersphere.services.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {

    // Default for a notification type the user has never customized: in-app visible, but no
    // simulated email/SMS "send" - see NotificationServiceImpl.MULTI_CHANNEL_TYPES for which
    // types are even eligible to attempt email/SMS in the first place. WEB_PUSH defaults off
    // too, same as email/SMS - the frontend enables it once the user grants browser push
    // permission and registers a subscription (see PushSubscriptionService).
    static final boolean DEFAULT_IN_APP_ENABLED = true;
    static final boolean DEFAULT_EMAIL_ENABLED = false;
    static final boolean DEFAULT_SMS_ENABLED = false;
    static final boolean DEFAULT_WEB_PUSH_ENABLED = false;

    private final NotificationPreferenceDao notificationPreferenceDao;

    @Override
    public List<NotificationPreferenceDTO> getMine(Long userId) {
        Map<NotificationTypeEnum, NotificationPreference> existing = new HashMap<>();
        for (NotificationPreference preference : notificationPreferenceDao.findByUserId(userId)) {
            existing.put(preference.getNotificationType(), preference);
        }

        return List.of(NotificationTypeEnum.values()).stream()
                .map(type -> toResponse(type, existing.get(type)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<NotificationPreferenceDTO> updateMine(Long userId, List<NotificationPreferenceDTO> updates) {
        for (NotificationPreferenceDTO update : updates) {
            NotificationPreference preference = notificationPreferenceDao.findByUserIdAndType(userId, update.getType())
                    .orElse(NotificationPreference.builder()
                            .userId(userId)
                            .notificationType(update.getType())
                            .build());
            preference.setInAppEnabled(update.isInAppEnabled());
            preference.setEmailEnabled(update.isEmailEnabled());
            preference.setSmsEnabled(update.isSmsEnabled());
            preference.setWebPushEnabled(update.isWebPushEnabled());
            notificationPreferenceDao.save(preference);
        }
        return getMine(userId);
    }

    private NotificationPreferenceDTO toResponse(NotificationTypeEnum type, NotificationPreference preference) {
        if (preference == null) {
            return NotificationPreferenceDTO.builder()
                    .type(type)
                    .inAppEnabled(DEFAULT_IN_APP_ENABLED)
                    .emailEnabled(DEFAULT_EMAIL_ENABLED)
                    .smsEnabled(DEFAULT_SMS_ENABLED)
                    .webPushEnabled(DEFAULT_WEB_PUSH_ENABLED)
                    .build();
        }
        return NotificationPreferenceDTO.builder()
                .type(type)
                .inAppEnabled(preference.isInAppEnabled())
                .emailEnabled(preference.isEmailEnabled())
                .smsEnabled(preference.isSmsEnabled())
                .webPushEnabled(preference.isWebPushEnabled())
                .build();
    }
}
