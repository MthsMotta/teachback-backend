package br.com.teachback.backend.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CacheConfig {

    @Bean
    public Cache<String, Integer> loginAttemptsCache(){
        return Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(20))
                .build();
    }
}
