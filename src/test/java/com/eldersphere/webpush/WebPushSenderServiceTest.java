package com.eldersphere.webpush;

import com.eldersphere.dao.notification.NotificationDeliveryLogDao;
import com.eldersphere.dao.notification.PushSubscriptionDao;
import com.eldersphere.entities.Notification;
import com.eldersphere.entities.NotificationDeliveryLog;
import com.eldersphere.entities.PushSubscription;
import com.eldersphere.enums.NotificationChannelEnum;
import com.eldersphere.enums.NotificationTypeEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link WebPushSenderService}. The actual HTTP send is mocked out (via a
 * mocked {@link PushService}) so these exercise only this class's own logic: delivery logging
 * on a successful send, automatic removal of a subscription the push service reports as gone
 * (404/410), and - most importantly - that every kind of send failure (network error,
 * unexpected status) is swallowed here rather than propagated, since a single dead browser
 * subscription must never break the notification-creation flow that triggered it.
 */
class WebPushSenderServiceTest {

    private static final Long USER_ID = 10L;

    private PushService pushService;
    private PushSubscriptionDao pushSubscriptionDao;
    private NotificationDeliveryLogDao notificationDeliveryLogDao;
    private WebPushSenderService webPushSenderService;

    @BeforeEach
    void setUp() {
        pushService = mock(PushService.class);
        pushSubscriptionDao = mock(PushSubscriptionDao.class);
        notificationDeliveryLogDao = mock(NotificationDeliveryLogDao.class);
        webPushSenderService = new WebPushSenderService(
                pushService, pushSubscriptionDao, notificationDeliveryLogDao, new ObjectMapper());
    }

    @Test
    void sendToUser_noRegisteredSubscriptions_neverCallsThePushService() throws Exception {
        when(pushSubscriptionDao.findByUserId(USER_ID)).thenReturn(List.of());

        webPushSenderService.sendToUser(notification(), USER_ID);

        verifyNoInteractions(pushService);
        verifyNoInteractions(notificationDeliveryLogDao);
    }

    @Test
    void sendToUser_successfulSend_logsAWebPushDeliveryRow() throws Exception {
        PushSubscription subscription = subscription(1L);
        HttpResponse response = httpResponseWithStatus(201);
        when(pushSubscriptionDao.findByUserId(USER_ID)).thenReturn(List.of(subscription));
        when(pushService.send(any())).thenReturn(response);

        webPushSenderService.sendToUser(notification(), USER_ID);

        ArgumentCaptor<NotificationDeliveryLog> captor = ArgumentCaptor.forClass(NotificationDeliveryLog.class);
        verify(notificationDeliveryLogDao, times(1)).save(captor.capture());
        assertThat(captor.getValue().getChannel()).isEqualTo(NotificationChannelEnum.WEB_PUSH);
        assertThat(captor.getValue().getSimulatedRecipient()).isEqualTo(subscription.getEndpoint());
        verify(pushSubscriptionDao, never()).deleteById(any());
    }

    @Test
    void sendToUser_pushServiceReports404_removesStaleSubscriptionWithoutLogging() throws Exception {
        PushSubscription subscription = subscription(2L);
        HttpResponse response = httpResponseWithStatus(404);
        when(pushSubscriptionDao.findByUserId(USER_ID)).thenReturn(List.of(subscription));
        when(pushService.send(any())).thenReturn(response);

        webPushSenderService.sendToUser(notification(), USER_ID);

        verify(pushSubscriptionDao, times(1)).deleteById(2L);
        verify(notificationDeliveryLogDao, never()).save(any());
    }

    @Test
    void sendToUser_pushServiceReports410Gone_removesStaleSubscription() throws Exception {
        PushSubscription subscription = subscription(3L);
        HttpResponse response = httpResponseWithStatus(410);
        when(pushSubscriptionDao.findByUserId(USER_ID)).thenReturn(List.of(subscription));
        when(pushService.send(any())).thenReturn(response);

        webPushSenderService.sendToUser(notification(), USER_ID);

        verify(pushSubscriptionDao, times(1)).deleteById(3L);
        verify(notificationDeliveryLogDao, never()).save(any());
    }

    @Test
    void sendToUser_sendThrowsAnException_failsSoftAndNeverPropagates() throws Exception {
        PushSubscription subscription = subscription(4L);
        when(pushSubscriptionDao.findByUserId(USER_ID)).thenReturn(List.of(subscription));
        when(pushService.send(any())).thenThrow(new IOException("connection refused"));

        // The call below must complete normally - this is exactly what protects a real
        // notification-creation flow (e.g. a booking status change) from an unreachable or
        // malformed push subscription blowing up the whole request.
        webPushSenderService.sendToUser(notification(), USER_ID);

        verify(notificationDeliveryLogDao, never()).save(any());
        verify(pushSubscriptionDao, never()).deleteById(any());
    }

    @Test
    void sendToUser_unexpectedServerErrorStatus_failsSoftWithoutDeletingOrLogging() throws Exception {
        PushSubscription subscription = subscription(5L);
        HttpResponse response = httpResponseWithStatus(500);
        when(pushSubscriptionDao.findByUserId(USER_ID)).thenReturn(List.of(subscription));
        when(pushService.send(any())).thenReturn(response);

        webPushSenderService.sendToUser(notification(), USER_ID);

        verify(notificationDeliveryLogDao, never()).save(any());
        verify(pushSubscriptionDao, never()).deleteById(any());
    }

    @Test
    void sendToUser_multipleSubscriptions_eachHandledIndependently() throws Exception {
        PushSubscription stale = subscription(6L);
        PushSubscription healthy = subscription(7L);
        HttpResponse staleResponse = httpResponseWithStatus(410);
        HttpResponse healthyResponse = httpResponseWithStatus(201);
        when(pushSubscriptionDao.findByUserId(USER_ID)).thenReturn(List.of(stale, healthy));
        when(pushService.send(any()))
                .thenReturn(staleResponse)
                .thenReturn(healthyResponse);

        webPushSenderService.sendToUser(notification(), USER_ID);

        verify(pushSubscriptionDao, times(1)).deleteById(6L);
        verify(notificationDeliveryLogDao, times(1)).save(any());
    }

    private static Notification notification() {
        return Notification.builder()
                .id(100L)
                .userId(USER_ID)
                .type(NotificationTypeEnum.GENERAL)
                .title("Title")
                .message("Message")
                .build();
    }

    // A syntactically valid (real, unpadded base64url, uncompressed P-256 point) but otherwise
    // meaningless public key. The nl.martijndwars.webpush.Notification constructor decodes
    // p256dh as an EC point *before* pushService.send(...) (mocked, below) is ever reached, so
    // it must actually parse - unlike a garbage placeholder string, which fails right there and
    // would make every test below exercise the "bad subscription data" path instead of the
    // "push service returned X" path each test is named for.
    private static final String DUMMY_P256DH = "BN4cNJBWEhzzZ0hlqWU6fV-CcEtrGCxrZSjsjbkDHCS5q1mXeW58hBT_3yarqsbWJsuitV3_WXrYgzqBFGJRF-0";
    private static final String DUMMY_AUTH = "dGVzdC1hdXRoLXNlY3JldA";

    private static PushSubscription subscription(Long id) {
        return PushSubscription.builder()
                .id(id)
                .userId(USER_ID)
                .endpoint("https://push.example.com/ep/" + id)
                .p256dhKey(DUMMY_P256DH)
                .authKey(DUMMY_AUTH)
                .build();
    }

    private static HttpResponse httpResponseWithStatus(int status) {
        HttpResponse response = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        when(statusLine.getStatusCode()).thenReturn(status);
        when(response.getStatusLine()).thenReturn(statusLine);
        return response;
    }
}
