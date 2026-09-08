package com.eldersphere.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Low-cardinality, rarely-changing lookups (public landing page content) get a short TTL
 * cache so repeat hits skip the DB round trip without needing write-path eviction wired
 * through every admin edit.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    private static final List<String> CACHE_NAMES = List.of("landingPagePublic");

    @Bean
    public CacheManager cacheManager() {
        List<Cache> caches = new ArrayList<>();
        for (String name : CACHE_NAMES) {
            caches.add(buildCache(name, 5, TimeUnit.MINUTES, 50));
        }
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(caches);
        return cacheManager;
    }

    private CaffeineCache buildCache(String name, long ttl, TimeUnit unit, int maximumSize) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .expireAfterWrite(ttl, unit)
                .maximumSize(maximumSize)
                .build());
    }
}
