package dev.guillemdiaz.herald.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Only applies the rate limiter to the endpoint that actually
        // dispatches SMS
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/messages/send");
    }
}