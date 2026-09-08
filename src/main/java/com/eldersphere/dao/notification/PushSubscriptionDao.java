package com.eldersphere.dao.notification;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.PushSubscription;
import com.eldersphere.repositories.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PushSubscriptionDao implements IDao<PushSubscription, Long> {

    private final PushSubscriptionRepository pushSubscriptionRepository;

    @Override
    public JpaRepository<PushSubscription, Long> getRepository() {
        return pushSubscriptionRepository;
    }

    public PushSubscription save(PushSubscription subscription) {
        return pushSubscriptionRepository.save(subscription);
    }

    public List<PushSubscription> findByUserId(Long userId) {
        return pushSubscriptionRepository.findByUserId(userId);
    }

    public Optional<PushSubscription> findByEndpoint(String endpoint) {
        return pushSubscriptionRepository.findByEndpoint(endpoint);
    }

    @Transactional
    public void deleteByUserIdAndEndpoint(Long userId, String endpoint) {
        pushSubscriptionRepository.deleteByUserIdAndEndpoint(userId, endpoint);
    }

    @Transactional
    public void deleteById(Long id) {
        pushSubscriptionRepository.deleteById(id);
    }
}
