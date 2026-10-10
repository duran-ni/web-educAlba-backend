package dev.duran.web_educAlba_backend.academy;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Datos de entrada del formulario público de Contacto
public record ContactMessageRequest(

    @NotBlank
    String name,

    @NotBlank
    @Email
    String email,

    @NotBlank
    String subject,

    @NotBlank
    String message
) {

}
