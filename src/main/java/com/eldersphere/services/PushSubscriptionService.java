package com.eldersphere.services;

import com.eldersphere.dtos.Notification.PushSubscriptionRequest;

public interface PushSubscriptionService {

    /**
     * Registers (or, if the endpoint already exists, updates the ownership/keys of) a
     * browser's push subscription for the given user. Idempotent on {@code endpoint}.
     */
    void register(Long userId, PushSubscriptionRequest request);

    /**
     * Removes the caller's subscription for the given endpoint. A no-op if no such
     * subscription exists (e.g. it was already cleaned up as stale) - unsubscribe is
     * inherently idempotent.
     */
    void unsubscribe(Long userId, String endpoint);
}
