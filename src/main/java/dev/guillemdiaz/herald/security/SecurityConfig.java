package dev.guillemdiaz.herald.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disables CSRF protection - not required for stateless
                // JWT-based APIs
                .csrf(AbstractHttpConfigurer::disable)
                // Enforce stateless session management - no server-side
                // session is created or used, each request must carry a
                // valid JWT
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Defines endpoint routing rules
                .authorizeHttpRequests(auth -> auth
                        // Anyone can log in/register
                        .requestMatchers("/api/auth/**").permitAll()
                        // Documentation is public
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Everything else requires a valid JWT
                        .anyRequest().authenticated()
                );

        // TODO: inject custom JwtAuthenticationFilter

        return http.build();
    }

    // Required to manually trigger authentication in AuthController
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Uses BCrypt by default for new saves, but still understands {noop} for
    // the data.sql
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}