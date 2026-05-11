package dev.guillemdiaz.herald.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Profile("test")
public class InMemoryRateLimitingService implements RateLimitingService {

    private final Map<Long, Bucket> cache = new ConcurrentHashMap<>();

    @Override
    public Bucket resolveBucket(Long tenantId) {
        return cache.computeIfAbsent(tenantId, this::newBucket);
    }

    private Bucket newBucket(Long tenantId) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(5)
                .refillIntervally(5, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}