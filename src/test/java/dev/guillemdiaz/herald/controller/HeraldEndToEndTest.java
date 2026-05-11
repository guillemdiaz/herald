package dev.guillemdiaz.herald.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.guillemdiaz.herald.dto.MessageRequest;
import dev.guillemdiaz.herald.dto.RegisterRequest;
import dev.guillemdiaz.herald.repository.MessageLogRepository;
import dev.guillemdiaz.herald.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") // Tells Spring to use application-test.yml
class HeraldEndToEndTest {

    private MockMvc mockMvc;

    // Converts Java objects to JSON
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private MessageLogRepository messageLogRepository;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        messageLogRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    @Test
    void fullUserJourney_Register_Send_And_ViewHistory() throws Exception {
        // Registers the tenant
        RegisterRequest registerRequest = new RegisterRequest("E2E Corp",
                "e2e@test.com", "Password123!");

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        // Extracts the token using regex
        String responseString = registerResult.getResponse().getContentAsString();
        // Grabs the actual JWT string
        String token = responseString.split("\"")[3];
        String bearerToken = "Bearer " + token;

        // Sends a message
        MessageRequest messageRequest = new MessageRequest("+34601100300",
                "E2E Test " +
                "Message");

        mockMvc.perform(post("/api/messages/send")
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.recipientNumber").value("+34601100300"));

        // Proves it actually hit the database
        assertEquals(1, messageLogRepository.findAll().size());

        // Views history
        mockMvc.perform(get("/api/messages/history")
                        .header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("E2E Test Message"));
    }

    @Test
    void rateLimiter_BlocksRequests_AfterCapacityExceeded() throws Exception {
        // Registers
        RegisterRequest registerRequest = new RegisterRequest("Spammer Inc",
                "spam@test.com", "Password123!");
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andReturn();

        String token = result.getResponse().getContentAsString().split("\"")[3];
        String bearerToken = "Bearer " + token;

        // Spams the api
        MessageRequest messageRequest = new MessageRequest("+34700100200",
                "Spam");
        String jsonPayload = objectMapper.writeValueAsString(messageRequest);

        // Sends 5 successful requests
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/messages/send")
                            .header("Authorization", bearerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonPayload))
                    .andExpect(status().isOk());
        }

        // The 6th request has to fail
        mockMvc.perform(post("/api/messages/send")
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isTooManyRequests()) // Expects 429 Status
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void globalExceptionHandler_CatchesBadDtoValidation() throws Exception {
        // Sends a request with a completely missing phone number (Triggers
        // @NotBlank)
        RegisterRequest badRequest = new RegisterRequest("", "not-an-email",
                "");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest()) // Expect 400
                // Expects a map of the bad fields from the ExceptionHandler
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.companyName").exists());
    }
}