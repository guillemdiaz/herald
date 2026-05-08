package dev.guillemdiaz.herald.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "{validation.register.company.required}")
        String companyName,

        @NotBlank(message = "{validation.register.email.required}")
        @Email(message = "{validation.register.email.format}")
        String email,

        @NotBlank(message = "{validation.register.password.required}")
        @Size(min = 6, message = "{validation.register.password.format}")
        String password
) {}
