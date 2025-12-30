package com.anonchat.backend.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    // Store buckets in memory (Map<IP_Address, Bucket>)
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public boolean allowRequest(String ipAddress) {
        Bucket bucket = cache.computeIfAbsent(ipAddress, this::createNewBucket);
        return bucket.tryConsume(1);
    }

    private Bucket createNewBucket(String ipAddress) {
        // Rule: Allow 5 messages initially
        // Refill: Add 1 token every 2 seconds
        return Bucket.builder()
                .addLimit(Bandwidth.classic(5, Refill.greedy(1, Duration.ofSeconds(2))))
                .build();
    }
}