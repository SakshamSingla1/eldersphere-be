package com.eldersphere.services.impl;

import com.eldersphere.dao.notification.NotificationDao;
import com.eldersphere.dao.notification.NotificationDeliveryLogDao;
import com.eldersphere.dao.notification.NotificationPreferenceDao;
import com.eldersphere.dao.user.UserDao;
import com.eldersphere.entities.Notification;
import com.eldersphere.entities.NotificationDeliveryLog;
import com.eldersphere.entities.NotificationPreference;
import com.eldersphere.entities.User;
import com.eldersphere.enums.NotificationChannelEnum;
import com.eldersphere.enums.NotificationTypeEnum;
import com.eldersphere.webpush.WebPushSenderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the notification-preference gating added to {@link NotificationServiceImpl}
 * (Task 2): a user who has disabled a delivery channel for a notification type must genuinely
 * get no {@link NotificationDeliveryLog} row for it, proving preferences are wired into real
 * dispatch rather than just stored as inert data.
 */
class NotificationServiceImplTest {

    private static final Long USER_ID = 55L;

    private NotificationDao notificationDao;
    private NotificationDeliveryLogDao notificationDeliveryLogDao;
    private NotificationPreferenceDao notificationPreferenceDao;
    private UserDao userDao;
    private SimpMessagingTemplate messagingTemplate;
    private WebPushSenderService webPushSenderService;
    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        notificationDao = mock(NotificationDao.class);
        notificationDeliveryLogDao = mock(NotificationDeliveryLogDao.class);
        notificationPreferenceDao = mock(NotificationPreferenceDao.class);
        userDao = mock(UserDao.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        webPushSenderService = mock(WebPushSenderService.class);
        notificationService = new NotificationServiceImpl(notificationDao, notificationDeliveryLogDao, notificationPreferenceDao, userDao, messagingTemplate, webPushSenderService);

        when(notificationDao.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification n = invocation.getArgument(0);
            n.setId(1L);
            return n;
        });
        when(userDao.findById(eq(USER_ID), eq(true)))
                .thenReturn(User.builder().id(USER_ID).email("user@eldersphere.app").phone("+10000000000").build());
    }

    @Test
    void create_multiChannelType_noPreferenceRow_defaultsSuppressSimulatedEmailAndSms() {
        when(notificationPreferenceDao.findByUserIdAndType(USER_ID, NotificationTypeEnum.EMERGENCY_ALERT))
                .thenReturn(Optional.empty());

        notificationService.create(USER_ID, NotificationTypeEnum.EMERGENCY_ALERT, "Alert", "Help needed", null);

        ArgumentCaptor<NotificationDeliveryLog> captor = ArgumentCaptor.forClass(NotificationDeliveryLog.class);
        verify(notificationDeliveryLogDao, org.mockito.Mockito.times(1)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(NotificationDeliveryLog::getChannel)
                .containsExactly(NotificationChannelEnum.IN_APP);
    }

    @Test
    void create_multiChannelType_emailAndSmsExplicitlyEnabled_logsAllThreeChannels() {
        NotificationPreference enabled = NotificationPreference.builder()
                .userId(USER_ID)
                .notificationType(NotificationTypeEnum.EMERGENCY_ALERT)
                .inAppEnabled(true)
                .emailEnabled(true)
                .smsEnabled(true)
                .build();
        when(notificationPreferenceDao.findByUserIdAndType(USER_ID, NotificationTypeEnum.EMERGENCY_ALERT))
                .thenReturn(Optional.of(enabled));

        notificationService.create(USER_ID, NotificationTypeEnum.EMERGENCY_ALERT, "Alert", "Help needed", null);

        ArgumentCaptor<NotificationDeliveryLog> captor = ArgumentCaptor.forClass(NotificationDeliveryLog.class);
        verify(notificationDeliveryLogDao, org.mockito.Mockito.times(3)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(NotificationDeliveryLog::getChannel)
                .containsExactlyInAnyOrder(NotificationChannelEnum.IN_APP, NotificationChannelEnum.SIMULATED_SMS, NotificationChannelEnum.SIMULATED_EMAIL);
    }

    @Test
    void create_multiChannelType_emailDisabledAfterPreviouslyEnabled_suppressesOnlyEmail() {
        // Before: email enabled -> would have logged it (proven above). After: user disables
        // email for this type -> the same trigger must no longer produce a SIMULATED_EMAIL row.
        NotificationPreference emailDisabled = NotificationPreference.builder()
                .userId(USER_ID)
                .notificationType(NotificationTypeEnum.BOOKING_CONFIRMED)
                .inAppEnabled(true)
                .emailEnabled(false)
                .smsEnabled(true)
                .build();
        when(notificationPreferenceDao.findByUserIdAndType(USER_ID, NotificationTypeEnum.BOOKING_CONFIRMED))
                .thenReturn(Optional.of(emailDisabled));

        notificationService.create(USER_ID, NotificationTypeEnum.BOOKING_CONFIRMED, "Booking confirmed", "Your booking is confirmed", null);

        ArgumentCaptor<NotificationDeliveryLog> captor = ArgumentCaptor.forClass(NotificationDeliveryLog.class);
        verify(notificationDeliveryLogDao, org.mockito.Mockito.times(2)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(NotificationDeliveryLog::getChannel)
                .containsExactlyInAnyOrder(NotificationChannelEnum.IN_APP, NotificationChannelEnum.SIMULATED_SMS)
                .doesNotContain(NotificationChannelEnum.SIMULATED_EMAIL);
    }

    @Test
    void create_singleChannelType_ignoresEmailSmsPreferencesEntirely() {
        // GENERAL isn't in MULTI_CHANNEL_TYPES, so even an explicit opt-in to email/SMS must
        // not produce those delivery logs - only types eligible for multi-channel dispatch do.
        NotificationPreference allEnabled = NotificationPreference.builder()
                .userId(USER_ID)
                .notificationType(NotificationTypeEnum.GENERAL)
                .inAppEnabled(true)
                .emailEnabled(true)
                .smsEnabled(true)
                .build();
        when(notificationPreferenceDao.findByUserIdAndType(USER_ID, NotificationTypeEnum.GENERAL))
                .thenReturn(Optional.of(allEnabled));

        notificationService.create(USER_ID, NotificationTypeEnum.GENERAL, "FYI", "Just letting you know", null);

        ArgumentCaptor<NotificationDeliveryLog> captor = ArgumentCaptor.forClass(NotificationDeliveryLog.class);
        verify(notificationDeliveryLogDao, org.mockito.Mockito.times(1)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(NotificationDeliveryLog::getChannel)
                .containsExactly(NotificationChannelEnum.IN_APP);
    }

    @Test
    void create_inAppDisabled_suppressesInAppDeliveryLogToo() {
        NotificationPreference inAppDisabled = NotificationPreference.builder()
                .userId(USER_ID)
                .notificationType(NotificationTypeEnum.NEW_MESSAGE)
                .inAppEnabled(false)
                .emailEnabled(false)
                .smsEnabled(false)
                .build();
        when(notificationPreferenceDao.findByUserIdAndType(USER_ID, NotificationTypeEnum.NEW_MESSAGE))
                .thenReturn(Optional.of(inAppDisabled));

        notificationService.create(USER_ID, NotificationTypeEnum.NEW_MESSAGE, "New message", "You have a new message", null);

        verify(notificationDeliveryLogDao, never()).save(any());
    }

    @Test
    void create_webPushEnabled_dispatchesToWebPushSenderService() {
        // GENERAL isn't a MULTI_CHANNEL_TYPES type, proving WEB_PUSH gating is independent of
        // that SMS/email-only restriction - push should fire for any type the user opted into.
        NotificationPreference webPushEnabled = NotificationPreference.builder()
                .userId(USER_ID)
                .notificationType(NotificationTypeEnum.GENERAL)
                .inAppEnabled(true)
                .emailEnabled(false)
                .smsEnabled(false)
                .webPushEnabled(true)
                .build();
        when(notificationPreferenceDao.findByUserIdAndType(USER_ID, NotificationTypeEnum.GENERAL))
                .thenReturn(Optional.of(webPushEnabled));

        notificationService.create(USER_ID, NotificationTypeEnum.GENERAL, "FYI", "Just letting you know", null);

        verify(webPushSenderService, times(1)).sendToUser(any(Notification.class), eq(USER_ID));
    }

    @Test
    void create_webPushDisabledOrDefaulted_neverCallsWebPushSenderService() {
        when(notificationPreferenceDao.findByUserIdAndType(USER_ID, NotificationTypeEnum.GENERAL))
                .thenReturn(Optional.empty());

        notificationService.create(USER_ID, NotificationTypeEnum.GENERAL, "FYI", "Just letting you know", null);

        verifyNoInteractions(webPushSenderService);
    }
}
