package dev.guillemdiaz.herald.service;

import dev.guillemdiaz.herald.dto.MessageRequest;
import dev.guillemdiaz.herald.dto.MessageResponse;
import dev.guillemdiaz.herald.entity.MessageLog;
import dev.guillemdiaz.herald.entity.MessageStatus;
import dev.guillemdiaz.herald.entity.Tenant;
import dev.guillemdiaz.herald.repository.MessageLogRepository;
import dev.guillemdiaz.herald.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageLogRepository messageLogRepository;

    @Mock
    private NotificationSender notificationSender;

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private MessageService messageService;

    private Tenant testTenant;
    private MessageRequest testRequest;

    @BeforeEach
    void setUp() {
        testTenant = new Tenant();
        testTenant.setId(1L);
        testTenant.setEmail("test@company.com");
        testTenant.setCompanyName("Test Corp");

        testRequest = new MessageRequest("+34600100200", "Testing Herald");
    }

    @Test
    void sendMessage_WhenSuccessful_ReturnsSentStatus() {
        // Tells the fake Tenant DB to return testTenant when asked for
        // "test@company.com"
        when(tenantRepository.findByEmail("test@company.com")).thenReturn(Optional.of(testTenant));

        // Tells the fake NotificationSender to always return true (success)
        when(notificationSender.send(anyString(), anyString())).thenReturn(true);

        // When the service tries to save the log, just return the log it
        // tried to save
        when(messageLogRepository.save(any(MessageLog.class))).thenAnswer(invocation -> {
            MessageLog savedLog = invocation.getArgument(0);
            savedLog.setId(100L); // Fake a database ID generation
            return savedLog;
        });

        // Runs the actual service method
        MessageResponse response = messageService.sendMessage("test@company" +
                ".com", testRequest);

        // Verifies the results
        assertNotNull(response);
        assertEquals(MessageStatus.SENT, response.status());
        assertEquals("+34600100200", response.recipientNumber());

        // Verifies that the repository's save() method was called exactly one
        // time
        verify(messageLogRepository, times(1)).save(any(MessageLog.class));
    }

    @Test
    void sendMessage_WhenNotificationFails_ReturnsFailedStatus() {
        // Tells the fake Tenant DB to return testTenant
        when(tenantRepository.findByEmail("test@company.com"))
                .thenReturn(Optional.of(testTenant));

        // Simulates the SMS provider failing to send the message
        when(notificationSender.send(anyString(), anyString()))
                .thenReturn(false);

        // Simulates saving the MessageLog and assigning a database ID
        when(messageLogRepository.save(any(MessageLog.class)))
                .thenAnswer(invocation -> {
                    MessageLog savedLog = invocation.getArgument(0);
                    savedLog.setId(100L);
                    return savedLog;
                });

        // Runs the service method
        MessageResponse response =
                messageService.sendMessage("test@company.com", testRequest);

        // Verifies the response reflects the failure
        assertNotNull(response);
        assertEquals(MessageStatus.FAILED, response.status());
        assertEquals("+34600100200", response.recipientNumber());
        // Verifies that the message was still saved to the database
        verify(messageLogRepository, times(1))
                .save(any(MessageLog.class));
        // Verifies that the notification sender was called once
        verify(notificationSender, times(1))
                .send(testRequest.recipientNumber(), testRequest.content());
    }

    @Test
    void sendMessage_WhenTenantNotFound_ThrowsIllegalArgumentException() {
        // Simulates the tenant not existing in the database
        when(tenantRepository.findByEmail("missing@company.com"))
                .thenReturn(Optional.empty());

        // Verifies that the service throws the expected exception
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> messageService.sendMessage(
                        "missing@company.com",
                        testRequest
                )
        );
        // Verifies the exception message
        assertEquals("Tenant not found", exception.getMessage());
        // Verifies that no message was sent
        verify(notificationSender, never())
                .send(anyString(), anyString());
        // Verifies that nothing was saved to the database
        verify(messageLogRepository, never())
                .save(any(MessageLog.class));
    }

    @Test
    void getMessageById_WhenMessageBelongsToDifferentTenant_ThrowsException() {
        // Simulates finding the logged-in user (Hacker)
        when(tenantRepository.findByEmail("hacker@company.com")).thenReturn(Optional.of(testTenant));

        // Simulates the database returning empty when asked for Message ID #99
        // specifically for this hacker's tenant ID (even if message 99
        // exists for someone else).
        when(messageLogRepository.findByIdAndTenantId(99L, testTenant.getId()))
                .thenReturn(Optional.empty());

        // Expects the service to throw an IllegalArgumentException
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> messageService.getMessageById("hacker@company.com", 99L)
        );

        // Verifies the exact error message matches what the
        // GlobalExceptionHandler expects
        assertEquals("Message not found or you do not have permission to view" +
                " it.", exception.getMessage());
    }

    @Test
    void getMessageHistory_ReturnsListOfMessages() {
        when(tenantRepository.findByEmail("test@company.com")).thenReturn(Optional.of(testTenant));

        // Creates a couple of fake saved messages
        MessageLog log1 = new MessageLog();
        log1.setId(1L);
        log1.setRecipientNumber("+111");

        MessageLog log2 = new MessageLog();
        log2.setId(2L);
        log2.setRecipientNumber("+222");

        // Tells the DB to return a List containing the two fake logs
        when(messageLogRepository.findAllByTenantId(testTenant.getId())).thenReturn(java.util.List.of(log1, log2));

        // Runs the service method
        java.util.List<MessageResponse> history =
                messageService.getMessageHistory("test@company.com");

        // Verifies the results
        assertEquals(2, history.size());
        assertEquals("+111", history.get(0).recipientNumber());
        assertEquals("+222", history.get(1).recipientNumber());
    }

}