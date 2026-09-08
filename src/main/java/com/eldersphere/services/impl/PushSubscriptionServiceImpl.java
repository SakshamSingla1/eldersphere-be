package com.eldersphere.services.impl;

import com.eldersphere.dao.notification.PushSubscriptionDao;
import com.eldersphere.dtos.Notification.PushSubscriptionRequest;
import com.eldersphere.entities.PushSubscription;
import com.eldersphere.services.PushSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PushSubscriptionServiceImpl implements PushSubscriptionService {

    private final PushSubscriptionDao pushSubscriptionDao;

    @Override
    @Transactional
    public void register(Long userId, PushSubscriptionRequest request) {
        PushSubscription subscription = pushSubscriptionDao.findByEndpoint(request.getEndpoint())
                .orElseGet(() -> PushSubscription.builder()
                        .endpoint(request.getEndpoint())
                        .build());
        // Re-registering the same endpoint (page reload, browser re-subscribing after a key
        // rotation, or a different logged-in user on a shared browser) simply overwrites the
        // owner/keys in place rather than erroring - registration is idempotent on endpoint.
        subscription.setUserId(userId);
        subscription.setP256dhKey(request.getKeys().getP256dh());
        subscription.setAuthKey(request.getKeys().getAuth());
        pushSubscriptionDao.save(subscription);
    }

    @Override
    @Transactional
    public void unsubscribe(Long userId, String endpoint) {
        pushSubscriptionDao.deleteByUserIdAndEndpoint(userId, endpoint);
    }
}
