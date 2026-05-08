package dev.guillemdiaz.herald.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

    // An in-memory cache mapping tenantId -> Bucket
    private final Map<Long, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(Long tenantId) {
        return cache.computeIfAbsent(tenantId, this::newBucket);
    }

    private Bucket newBucket(Long tenantId) {
        // Allows 5 requests per minute
        Bandwidth limit = Bandwidth.builder()
                .capacity(5)
                .refillGreedy(5, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}