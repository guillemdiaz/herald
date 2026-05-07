package dev.guillemdiaz.herald.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MessageRequest(
        @NotBlank(message = "Recipient phone number is required")
        @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Phone number must be in E.164 format (e.g., +34652123456)")
        String recipientNumber,

        @NotBlank(message = "Message payload cannot be empty")
        String content
) {}