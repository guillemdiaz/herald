package dev.guillemdiaz.herald.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "{validation.login.email.required}")
        @Email(message = "{validation.login.email.format}")
        String email,

        @NotBlank(message = "{validation.login.password.required}")
        String password
) {}
