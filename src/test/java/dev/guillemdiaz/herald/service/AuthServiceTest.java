package dev.guillemdiaz.herald.service;

import dev.guillemdiaz.herald.dto.AuthRequest;
import dev.guillemdiaz.herald.dto.RegisterRequest;
import dev.guillemdiaz.herald.entity.Tenant;
import dev.guillemdiaz.herald.repository.TenantRepository;
import dev.guillemdiaz.herald.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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

    @Test
    void register_WhenValid_SavesTenantAndReturnsToken() {
        RegisterRequest request = new RegisterRequest("Wayne Enterprises",
                "bruce@wayne.com", "Password123!");

        // Simulates the database confirming the email does not exist
        when(tenantRepository.existsByEmail("bruce@wayne.com")).thenReturn(false);
        // Simulates the password being hashed
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded_password");
        // Simulates generating the JWT
        when(jwtService.generateToken(any(), eq("bruce@wayne.com"))).thenReturn("mocked_jwt_token");

        // Runs the actual service method
        String token = authService.register(request);

        // Verifies the results
        assertEquals("mocked_jwt_token", token);
        verify(tenantRepository, times(1)).save(any(Tenant.class));
    }

    @Test
    void register_WhenEmailExists_ThrowsException() {
        RegisterRequest request = new RegisterRequest("Joker Inc", "bruce" +
                "@wayne.com", "something123");

        // Simulates the database saying the email is taken
        when(tenantRepository.existsByEmail("bruce@wayne.com")).thenReturn(true);

        // Acts and asserts
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request)
        );

        assertEquals("Email already in use", exception.getMessage());
        // Verifies that a duplicate user is never saved to the database
        verify(tenantRepository, never()).save(any(Tenant.class));
    }

    @Test
    void login_WhenValidCredentials_ReturnsToken() {
        AuthRequest request = new AuthRequest("bruce@wayne.com","Password123!");

        // Simulates finding the user in the database after successful
        // authentication
        when(tenantRepository.findByEmail("bruce@wayne.com")).thenReturn(Optional.of(testTenant));

        // Simulates generating the JWT
        when(jwtService.generateToken(testTenant.getId(), testTenant.getEmail())).thenReturn("mocked_login_token");

        // Runs the actual service method
        String token = authService.login(request);

        // Verifies the results
        assertEquals("mocked_login_token", token);
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_WhenUserNotFound_ThrowsException() {
        AuthRequest request = new AuthRequest("nobody@wayne.com",
                "wrongpassword");

        // Simulates the DB returning empty when looking up the email
        when(tenantRepository.findByEmail("nobody@wayne.com")).thenReturn(Optional.empty());

        // Asserts that the service throws the expected exception
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(request)
        );

        assertEquals("Invalid email or password", exception.getMessage());
        // Verifies a token for a fake user is not generated
        verify(jwtService, never()).generateToken(any(), anyString());
    }
}