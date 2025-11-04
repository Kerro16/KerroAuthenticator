package com.kerro.kerroauthenticator.jwt;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    // token -> expiryEpochMillis
    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    public void blacklistToken(String token, long expiryEpochMillis) {
        if (token == null || token.isBlank()) return;
        blacklist.put(token, expiryEpochMillis);
    }

    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) return false;
        Long expiry = blacklist.get(token);
        if (expiry == null) return false;
        if (expiry <= Instant.now().toEpochMilli()) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }

    // Limpieza periódica de tokens expirados (cada 5 minutos)
    @Scheduled(fixedDelayString = "PT5M")
    public void cleanup() {
        long now = Instant.now().toEpochMilli();
        blacklist.entrySet().removeIf(entry -> entry.getValue() <= now);
    }
}