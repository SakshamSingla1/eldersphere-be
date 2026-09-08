package com.eldersphere.webpush;

import com.eldersphere.dao.notification.NotificationDeliveryLogDao;
import com.eldersphere.dao.notification.PushSubscriptionDao;
import com.eldersphere.entities.Notification;
import com.eldersphere.entities.NotificationDeliveryLog;
import com.eldersphere.entities.PushSubscription;
import com.eldersphere.enums.NotificationChannelEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sends real, VAPID-authenticated Web Push messages to a user's registered browser
 * subscriptions (see {@link PushSubscription}) for a just-created {@link Notification}, and
 * records a {@link NotificationDeliveryLog} row for each one actually accepted by the push
 * service.
 * <p>
 * Deliberately fails soft: a subscription that is unreachable, expired, or otherwise rejected
 * must never propagate an exception up into the notification-creation flow that triggered it -
 * that flow (and its other delivery channels) must succeed regardless of whether any given
 * browser is still listening. A subscription the push service reports as gone (HTTP 404/410,
 * the standard "unsubscribe yourself" signal from the Push API spec) is deleted automatically.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebPushSenderService {

    private final PushService pushService;
    private final PushSubscriptionDao pushSubscriptionDao;
    private final NotificationDeliveryLogDao notificationDeliveryLogDao;
    private final ObjectMapper objectMapper;

    public void sendToUser(Notification notification, Long userId) {
        List<PushSubscription> subscriptions = pushSubscriptionDao.findByUserId(userId);
        if (subscriptions.isEmpty()) {
            return;
        }

        String payload = buildPayload(notification);
        LocalDateTime now = LocalDateTime.now();
        for (PushSubscription subscription : subscriptions) {
            sendOne(notification, subscription, payload, now);
        }
    }

    private void sendOne(Notification notification, PushSubscription subscription, String payload, LocalDateTime now) {
        try {
            nl.martijndwars.webpush.Subscription webPushSubscription = new nl.martijndwars.webpush.Subscription(
                    subscription.getEndpoint(),
                    new nl.martijndwars.webpush.Subscription.Keys(subscription.getP256dhKey(), subscription.getAuthKey()));
            nl.martijndwars.webpush.Notification pushNotification =
                    new nl.martijndwars.webpush.Notification(webPushSubscription, payload);

            HttpResponse response = pushService.send(pushNotification);
            int status = response.getStatusLine().getStatusCode();

            if (status == 404 || status == 410) {
                log.info("Web push subscription {} is stale (push service returned {}) - removing it",
                        subscription.getId(), status);
                pushSubscriptionDao.deleteById(subscription.getId());
                return;
            }

            if (status >= 200 && status < 300) {
                notificationDeliveryLogDao.save(NotificationDeliveryLog.builder()
                        .notificationId(notification.getId())
                        .channel(NotificationChannelEnum.WEB_PUSH)
                        .simulatedRecipient(subscription.getEndpoint())
                        .dispatchedAt(now)
                        .build());
                return;
            }

            log.warn("Web push to subscription {} returned unexpected status {}", subscription.getId(), status);
        } catch (Exception e) {
            log.warn("Web push send failed for subscription {} (endpoint {}): {}",
                    subscription.getId(), subscription.getEndpoint(), e.getMessage());
        }
    }

    private String buildPayload(Notification notification) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("notificationId", notification.getId());
        body.put("type", notification.getType() != null ? notification.getType().name() : null);
        body.put("title", notification.getTitle());
        body.put("message", notification.getMessage());
        body.put("link", notification.getLink());
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize web push payload for notification {}: {}", notification.getId(), e.getMessage());
            return "{\"title\":\"" + notification.getTitle() + "\"}";
        }
    }
}
