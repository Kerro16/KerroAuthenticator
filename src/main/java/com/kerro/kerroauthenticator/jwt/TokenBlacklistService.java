package com.kerro.kerroauthenticator.jwt;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ValueOperations<String, String> valueOps;

    public TokenBlacklistService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.valueOps = redisTemplate.opsForValue();
    }

    public void blacklistToken(String token, long expiryEpochMillis) {
        if (token == null || token.isBlank()) return;

        long now = System.currentTimeMillis();
        long ttlMillis = Math.max(expiryEpochMillis - now, 0);
        Duration ttl = Duration.ofMillis(ttlMillis > 0 ? ttlMillis : 3600000); // 1h fallback

        valueOps.set(token, "BLACKLISTED", ttl);
    }

    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) return false;
        return redisTemplate.hasKey(token);
    }

    @Scheduled(fixedDelayString = "PT5M")
    public void cleanup() {
    }
}
