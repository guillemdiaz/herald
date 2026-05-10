package dev.guillemdiaz.herald.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "secretKey",
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration",
                1000 * 60 * 60); // 1 hour
    }

    @Test
    void generateToken_AndExtractClaims_WorksProperly() {
        // Runs the actual service method
        String token = jwtService.generateToken(99L, "batman@wayne.com");

        // Verifies the results
        assertNotNull(token);
        assertEquals("batman@wayne.com", jwtService.extractEmail(token),
                "Should extract the correct subject (email)");
        assertEquals(99L, jwtService.extractTenantId(token), "Should extract " +
                "the custom tenantId claim");
    }

    @Test
    void isTokenValid_WithCorrectUser_ReturnsTrue() {
        String token = jwtService.generateToken(1L, "superman@dailyplanet.com");
        assertTrue(jwtService.isTokenValid(token, "superman@dailyplanet.com"));
    }

    @Test
    void isTokenValid_WithWrongUser_ReturnsFalse() {
        String token = jwtService.generateToken(1L, "superman@dailyplanet.com");
        assertFalse(jwtService.isTokenValid(token, "lex@corp.com"), "A token " +
                "should be invalid if the email doesn't match");
    }
}