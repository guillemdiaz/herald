package dev.guillemdiaz.herald.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Profile("!test")
@RequiredArgsConstructor
public class RedisRateLimitingService implements RateLimitingService {

    private final LettuceBasedProxyManager<byte[]> proxyManager;

    @Override
    public Bucket resolveBucket(Long tenantId) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(5)
                .refillIntervally(5, Duration.ofMinutes(1))
                .build();

        BucketConfiguration configuration = BucketConfiguration.builder()
                .addLimit(limit).build();

        // Looks in Redis for the bucket. If it doesn't exist, it creates it
        // using the config above.
        return proxyManager.builder().build(tenantId.toString().getBytes(),
                () -> configuration);
    }
}