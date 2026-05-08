package dev.guillemdiaz.herald.config;

import dev.guillemdiaz.herald.security.JwtService;
import dev.guillemdiaz.herald.service.RateLimitingService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;
    private final RateLimitingService rateLimitingService;

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) throws Exception {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Lets SecurityFilterChain handle unauthenticated users
            return true;
        }

        try {
            // Extracts the JWT and gets the Tenant ID instantly (No DB call)
            String token = authHeader.substring(7);
            Long tenantId = jwtService.extractTenantId(token);

            // Finds their personal bucket
            Bucket bucket = rateLimitingService.resolveBucket(tenantId);

            // Tries to consume 1 token
            if (bucket.tryConsume(1)) {
                return true; // Allowed, proceeds to the Controller.
            } else {
                log.warn("Rate limit exceeded for tenant ID: {}", tenantId);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Too many requests. " +
                        "You are limited to 5 messages per minute.\"}");
                return false; // Blocked, does not hit the Controller.
            }
        } catch (Exception e) {
            log.error("Error processing rate limit", e);
            // If the rate limiter breaks, it doesn't break the whole API
            return true;
        }
    }
}