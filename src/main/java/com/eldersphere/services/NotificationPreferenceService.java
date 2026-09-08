package com.eldersphere.services;

import com.eldersphere.dtos.Notification.NotificationPreferenceDTO;

import java.util.List;

public interface NotificationPreferenceService {

    /**
     * One entry per {@link com.eldersphere.enums.NotificationTypeEnum} value for this user,
     * defaulting an entry the user has never customized to in-app on / email+SMS off.
     */
    List<NotificationPreferenceDTO> getMine(Long userId);

    /**
     * Upserts the given entries (only the types included are changed) and returns the full,
     * freshly-resolved list for every notification type.
     */
    List<NotificationPreferenceDTO> updateMine(Long userId, List<NotificationPreferenceDTO> updates);
}
