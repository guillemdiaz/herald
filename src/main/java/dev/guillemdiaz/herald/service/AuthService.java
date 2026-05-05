package dev.guillemdiaz.herald.service;

import dev.guillemdiaz.herald.dto.AuthRequest;
import dev.guillemdiaz.herald.dto.RegisterRequest;
import dev.guillemdiaz.herald.entity.Tenant;
import dev.guillemdiaz.herald.repository.TenantRepository;
import dev.guillemdiaz.herald.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public boolean emailExists(String email) {
        return tenantRepository.existsByEmail(email);
    }

    @Transactional // Ensures database safety
    public String register(RegisterRequest request) {
        Tenant tenant = new Tenant();
        tenant.setCompanyName(request.companyName());
        tenant.setEmail(request.email());
        tenant.setPassword(passwordEncoder.encode(request.password()));
        tenant.setActive(true);

        tenantRepository.save(tenant);
        return jwtService.generateToken(tenant.getId(), tenant.getEmail());
    }

    public String login(AuthRequest request) {
        // Validates the password against the encoded DB password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // If authentication passes, fetches the tenant to get their ID for
        // the JWT
        Tenant tenant = tenantRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        return jwtService.generateToken(tenant.getId(), tenant.getEmail());
    }
}