package dev.guillemdiaz.herald.service;

import io.github.bucket4j.Bucket;

public interface RateLimitingService {
    Bucket resolveBucket(Long tenantId);
}