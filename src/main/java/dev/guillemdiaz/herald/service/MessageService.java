package dev.guillemdiaz.herald.service;

import dev.guillemdiaz.herald.dto.MessageRequest;
import dev.guillemdiaz.herald.dto.MessageResponse;
import dev.guillemdiaz.herald.entity.MessageLog;
import dev.guillemdiaz.herald.entity.MessageStatus;
import dev.guillemdiaz.herald.entity.Tenant;
import dev.guillemdiaz.herald.repository.MessageLogRepository;
import dev.guillemdiaz.herald.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageLogRepository messageLogRepository;
    private final NotificationSender notificationSender;
    private final TenantRepository tenantRepository;

    @Transactional
    public MessageResponse sendMessage(String tenantEmail, MessageRequest request) {
        Tenant tenant = getTenant(tenantEmail);

        // Sends the actual SMS
        boolean isSent = notificationSender.send(request.recipientNumber(),
                request.content());

        // Creates the Database Record
        MessageLog logRecord = new MessageLog();
        logRecord.setTenant(tenant);
        logRecord.setRecipientNumber(request.recipientNumber());
        logRecord.setContent(request.content());
        logRecord.setStatus(isSent ? MessageStatus.SENT : MessageStatus.FAILED);

        logRecord = messageLogRepository.save(logRecord);

        return mapToResponse(logRecord);
    }

    public List<MessageResponse> getMessageHistory(String tenantEmail) {
        Tenant tenant = getTenant(tenantEmail);
        return messageLogRepository.findAllByTenantId(tenant.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public MessageResponse getMessageById(String tenantEmail, Long messageId) {
        Tenant tenant = getTenant(tenantEmail);
        MessageLog logRecord =
                messageLogRepository.findByIdAndTenantId(messageId, tenant.getId())
                .orElseThrow(() -> new IllegalArgumentException("Message not " +
                        "found or you do not have permission to view it."));
        return mapToResponse(logRecord);
    }

    private MessageResponse mapToResponse(MessageLog logRecord) {
        return new MessageResponse(
                logRecord.getId(),
                logRecord.getRecipientNumber(),
                logRecord.getContent(),
                logRecord.getStatus(),
                logRecord.getSentAt()
        );
    }

    private Tenant getTenant(String email) {
        return tenantRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
    }
}