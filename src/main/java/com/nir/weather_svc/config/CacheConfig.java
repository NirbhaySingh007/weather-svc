package com.nir.weather_svc.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.time.Duration;

@Configuration
public class CacheConfig {

    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(10))
                .recordStats();
    }

    @Bean
    public CacheManager cacheManager(
            Caffeine<Object, Object> caffeineConfig) {

        CaffeineCacheManager cacheManager =
                new CaffeineCacheManager("weather");

        cacheManager.setCaffeine(caffeineConfig);

        return cacheManager;
    }
}