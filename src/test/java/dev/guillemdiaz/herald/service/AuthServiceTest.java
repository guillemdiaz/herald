package dev.guillemdiaz.herald.service;

import dev.guillemdiaz.herald.entity.Tenant;
import dev.guillemdiaz.herald.repository.TenantRepository;
import dev.guillemdiaz.herald.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private Tenant testTenant;

    @BeforeEach
    void setUp() {
        testTenant = new Tenant();
        testTenant.setId(1L);
        testTenant.setCompanyName("Wayne Enterprises");
        testTenant.setEmail("bruce@wayne.com");
        testTenant.setPassword("encoded_password");
        testTenant.setActive(true);
    }
}