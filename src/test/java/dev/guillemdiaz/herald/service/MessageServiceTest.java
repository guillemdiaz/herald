package dev.guillemdiaz.herald.service;

import dev.guillemdiaz.herald.dto.MessageRequest;
import dev.guillemdiaz.herald.entity.Tenant;
import dev.guillemdiaz.herald.repository.MessageLogRepository;
import dev.guillemdiaz.herald.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

}