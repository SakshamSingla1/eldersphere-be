package com.eldersphere.services.impl;

import com.eldersphere.dao.notification.PushSubscriptionDao;
import com.eldersphere.dtos.Notification.PushSubscriptionKeysDTO;
import com.eldersphere.dtos.Notification.PushSubscriptionRequest;
import com.eldersphere.entities.PushSubscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PushSubscriptionServiceImpl}: registration must be idempotent on
 * {@code endpoint} (a browser re-registering, or re-subscribing with rotated keys, must update
 * the existing row rather than create a duplicate), and unsubscribe must scope the delete to
 * the caller's own subscription.
 */
class PushSubscriptionServiceImplTest {

    private static final Long USER_ID = 42L;
    private static final String ENDPOINT = "https://fcm.googleapis.com/fcm/send/abc123";

    private PushSubscriptionDao pushSubscriptionDao;
    private PushSubscriptionServiceImpl service;

    @BeforeEach
    void setUp() {
        pushSubscriptionDao = mock(PushSubscriptionDao.class);
        service = new PushSubscriptionServiceImpl(pushSubscriptionDao);
    }

    @Test
    void register_newEndpoint_createsSubscriptionForCaller() {
        when(pushSubscriptionDao.findByEndpoint(ENDPOINT)).thenReturn(Optional.empty());

        service.register(USER_ID, request("p256dh-key", "auth-key"));

        ArgumentCaptor<PushSubscription> captor = ArgumentCaptor.forClass(PushSubscription.class);
        verify(pushSubscriptionDao, times(1)).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(USER_ID);
        assertThat(captor.getValue().getEndpoint()).isEqualTo(ENDPOINT);
        assertThat(captor.getValue().getP256dhKey()).isEqualTo("p256dh-key");
        assertThat(captor.getValue().getAuthKey()).isEqualTo("auth-key");
    }

    @Test
    void register_sameEndpointAgain_isIdempotent_updatesInPlaceRatherThanDuplicating() {
        PushSubscription existing = PushSubscription.builder()
                .id(9L)
                .userId(USER_ID)
                .endpoint(ENDPOINT)
                .p256dhKey("old-p256dh")
                .authKey("old-auth")
                .build();
        when(pushSubscriptionDao.findByEndpoint(ENDPOINT)).thenReturn(Optional.of(existing));

        service.register(USER_ID, request("new-p256dh", "new-auth"));

        assertThat(existing.getP256dhKey()).isEqualTo("new-p256dh");
        assertThat(existing.getAuthKey()).isEqualTo("new-auth");
        verify(pushSubscriptionDao, times(1)).save(eq(existing));
        verify(pushSubscriptionDao, times(1)).save(any());
    }

    @Test
    void unsubscribe_deletesOnlyTheCallersSubscriptionForThatEndpoint() {
        service.unsubscribe(USER_ID, ENDPOINT);

        verify(pushSubscriptionDao, times(1)).deleteByUserIdAndEndpoint(USER_ID, ENDPOINT);
    }

    @Test
    void unsubscribe_neverThrowsWhenNothingToDelete_isIdempotent() {
        // deleteByUserIdAndEndpoint on a non-matching row is a normal no-op JPA derived delete -
        // the service must not first check existence and throw if absent.
        service.unsubscribe(USER_ID, "https://push.example.com/never-registered");

        verify(pushSubscriptionDao, never()).findByEndpoint(any());
    }

    private PushSubscriptionRequest request(String p256dh, String auth) {
        return PushSubscriptionRequest.builder()
                .endpoint(ENDPOINT)
                .keys(PushSubscriptionKeysDTO.builder().p256dh(p256dh).auth(auth).build())
                .build();
    }
}
