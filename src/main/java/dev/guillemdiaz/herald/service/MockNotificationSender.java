package dev.guillemdiaz.herald.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
public class MockNotificationSender implements NotificationSender{

    @Override
    public boolean send(String recipientNumber, String message) {
        // Fake message ID to make it look like a real API response
        String messageId = "msg_" + UUID.randomUUID().toString().replace("-",
                "").substring(0, 8);
        String payload = """
                {
                  "transaction_id": "%s",
                  "timestamp": "%s",
                  "type": "SMS",
                  "recipient": "%s",
                  "payload": "%s",
                  "status": 200,
                  "delivery_state": "DELIVERED_TO_CARRIER"
                }
                """.formatted(messageId, Instant.now(), recipientNumber,
                message);

        log.info("[HERALD MOCK PROVIDER] Mock SMS sent: \n{}", payload);

        return true;
    }
}
