package dev.guillemdiaz.herald.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MessageRequest(
        @NotBlank(message = "{validation.message.recipient.required}")
        @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "{validation" +
                ".message.recipient.format}")
        String recipientNumber,

        @NotBlank(message = "{validation.message.content.required}")
        String content
) {}