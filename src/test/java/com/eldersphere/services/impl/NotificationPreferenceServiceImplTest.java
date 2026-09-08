package com.eldersphere.services.impl;

import com.eldersphere.dao.notification.NotificationPreferenceDao;
import com.eldersphere.dtos.Notification.NotificationPreferenceDTO;
import com.eldersphere.entities.NotificationPreference;
import com.eldersphere.enums.NotificationTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link NotificationPreferenceServiceImpl}: a notification type the caller
 * has never customized must resolve to the documented default (in-app on, email/SMS off), and
 * a bulk update must upsert only the entries it's given while leaving the rest at default/as
 * previously set.
 */
class NotificationPreferenceServiceImplTest {

    private static final Long USER_ID = 77L;

    private NotificationPreferenceDao notificationPreferenceDao;
    private NotificationPreferenceServiceImpl service;

    @BeforeEach
    void setUp() {
        notificationPreferenceDao = mock(NotificationPreferenceDao.class);
        service = new NotificationPreferenceServiceImpl(notificationPreferenceDao);
    }

    @Test
    void getMine_noRowsYet_returnsOneDefaultedEntryPerNotificationType() {
        when(notificationPreferenceDao.findByUserId(USER_ID)).thenReturn(List.of());

        List<NotificationPreferenceDTO> result = service.getMine(USER_ID);

        assertThat(result).hasSize(NotificationTypeEnum.values().length);
        assertThat(result).allSatisfy(dto -> {
            assertThat(dto.isInAppEnabled()).isTrue();
            assertThat(dto.isEmailEnabled()).isFalse();
            assertThat(dto.isSmsEnabled()).isFalse();
            assertThat(dto.isWebPushEnabled()).isFalse();
        });
    }

    @Test
    void getMine_oneCustomizedType_mixesCustomAndDefaultEntries() {
        NotificationPreference customized = NotificationPreference.builder()
                .userId(USER_ID)
                .notificationType(NotificationTypeEnum.EMERGENCY_ALERT)
                .inAppEnabled(true)
                .emailEnabled(true)
                .smsEnabled(true)
                .webPushEnabled(true)
                .build();
        when(notificationPreferenceDao.findByUserId(USER_ID)).thenReturn(List.of(customized));

        List<NotificationPreferenceDTO> result = service.getMine(USER_ID);

        NotificationPreferenceDTO emergency = result.stream()
                .filter(dto -> dto.getType() == NotificationTypeEnum.EMERGENCY_ALERT)
                .findFirst().orElseThrow();
        assertThat(emergency.isEmailEnabled()).isTrue();
        assertThat(emergency.isSmsEnabled()).isTrue();
        assertThat(emergency.isWebPushEnabled()).isTrue();

        NotificationPreferenceDTO other = result.stream()
                .filter(dto -> dto.getType() == NotificationTypeEnum.GENERAL)
                .findFirst().orElseThrow();
        assertThat(other.isEmailEnabled()).isFalse();
        assertThat(other.isSmsEnabled()).isFalse();
    }

    @Test
    void updateMine_noExistingRow_createsAndSavesNewPreference() {
        when(notificationPreferenceDao.findByUserIdAndType(USER_ID, NotificationTypeEnum.BOOKING_CONFIRMED))
                .thenReturn(Optional.empty());
        when(notificationPreferenceDao.findByUserId(USER_ID)).thenReturn(List.of());

        NotificationPreferenceDTO update = NotificationPreferenceDTO.builder()
                .type(NotificationTypeEnum.BOOKING_CONFIRMED)
                .inAppEnabled(true)
                .emailEnabled(true)
                .smsEnabled(false)
                .build();

        service.updateMine(USER_ID, List.of(update));

        verify(notificationPreferenceDao).save(argThatMatchesUpdate());
    }

    @Test
    void updateMine_existingRow_updatesInPlaceRatherThanDuplicating() {
        NotificationPreference existing = NotificationPreference.builder()
                .id(5L)
                .userId(USER_ID)
                .notificationType(NotificationTypeEnum.BOOKING_CONFIRMED)
                .inAppEnabled(true)
                .emailEnabled(false)
                .smsEnabled(false)
                .build();
        when(notificationPreferenceDao.findByUserIdAndType(USER_ID, NotificationTypeEnum.BOOKING_CONFIRMED))
                .thenReturn(Optional.of(existing));
        when(notificationPreferenceDao.findByUserId(USER_ID)).thenReturn(List.of(existing));

        NotificationPreferenceDTO update = NotificationPreferenceDTO.builder()
                .type(NotificationTypeEnum.BOOKING_CONFIRMED)
                .inAppEnabled(true)
                .emailEnabled(true)
                .smsEnabled(true)
                .webPushEnabled(true)
                .build();

        service.updateMine(USER_ID, List.of(update));

        assertThat(existing.isEmailEnabled()).isTrue();
        assertThat(existing.isSmsEnabled()).isTrue();
        assertThat(existing.isWebPushEnabled()).isTrue();
        verify(notificationPreferenceDao).save(eq(existing));
    }

    private NotificationPreference argThatMatchesUpdate() {
        return org.mockito.ArgumentMatchers.argThat(p ->
                p.getUserId().equals(USER_ID)
                        && p.getNotificationType() == NotificationTypeEnum.BOOKING_CONFIRMED
                        && p.isEmailEnabled());
    }
}
