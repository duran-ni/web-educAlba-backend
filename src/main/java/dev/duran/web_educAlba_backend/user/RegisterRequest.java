package dev.duran.web_educAlba_backend.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Datos que llegan del formulario de registro del frontend
public record RegisterRequest(

    @NotBlank
    @Email
    String email,

    @NotBlank
    @Size (min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    String password
) {
    
}
