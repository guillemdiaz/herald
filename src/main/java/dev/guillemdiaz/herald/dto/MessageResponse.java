package dev.guillemdiaz.herald.dto;

import dev.guillemdiaz.herald.entity.MessageStatus;
import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        String recipientNumber,
        String content,
        MessageStatus status,
        LocalDateTime sentAt
) {}