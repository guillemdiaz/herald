package dev.guillemdiaz.herald.service;

import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitingServiceTest {

    private RateLimitingService rateLimitingService;

    @BeforeEach
    void setUp() {
        // Instantiates the real service because it only relies on
        // in-memory logic
        rateLimitingService = new RateLimitingService();
    }

    @Test
    void resolveBucket_AssignsUniqueBucketsToDifferentTenants() {
        // Runs the service method
        Bucket tenant1Bucket = rateLimitingService.resolveBucket(1L);
        Bucket tenant2Bucket = rateLimitingService.resolveBucket(2L);
        Bucket tenant1BucketAgain = rateLimitingService.resolveBucket(1L);

        // Verifies the results
        assertNotNull(tenant1Bucket);
        assertNotNull(tenant2Bucket);

        // Proves that Tenant 1 and Tenant 2 do not share the same rate limit
        // pool
        assertNotSame(tenant1Bucket, tenant2Bucket, "Tenants should have " +
                "completely separate buckets");

        // Proves the cache works, meaning fetching Tenant 1's bucket twice
        // returns the exact same object in memory
        assertSame(tenant1Bucket, tenant1BucketAgain, "Service should return " +
                "the cached bucket for the same tenant");
    }

    @Test
    void bucket_AllowsExactlyFiveRequestsBeforeBlocking() {
        // Runs the service method
        Bucket bucket = rateLimitingService.resolveBucket(99L);

        // The capacity is 5. The first 5 consumptions should return true
        assertTrue(bucket.tryConsume(1), "Request 1 should pass");
        assertTrue(bucket.tryConsume(1), "Request 2 should pass");
        assertTrue(bucket.tryConsume(1), "Request 3 should pass");
        assertTrue(bucket.tryConsume(1), "Request 4 should pass");
        assertTrue(bucket.tryConsume(1), "Request 5 should pass");

        // The 6th attempt should instantly return false because the bucket is
        // empty
        assertFalse(bucket.tryConsume(1), "Request 6 should be blocked");
    }
}