package com.eldersphere.config;

import com.eldersphere.webpush.VapidKeyProvider;
import lombok.RequiredArgsConstructor;
import nl.martijndwars.webpush.PushService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.GeneralSecurityException;

/**
 * Builds the single {@link PushService} bean used to send Web Push messages, keyed with the
 * VAPID key pair resolved by {@link VapidKeyProvider} (configured, or generated for local dev).
 */
@Configuration
@RequiredArgsConstructor
public class WebPushConfig {

    private final VapidKeyProvider vapidKeyProvider;

    @Bean
    public PushService pushService() throws GeneralSecurityException {
        return new PushService(vapidKeyProvider.getPublicKey(), vapidKeyProvider.getPrivateKey(), vapidKeyProvider.getSubject());
    }
}
