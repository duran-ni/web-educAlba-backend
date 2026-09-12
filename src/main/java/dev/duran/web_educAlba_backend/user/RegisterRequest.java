package dev.duran.web_educAlba_backend.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Datos que llegan del formulario de registro del frontend
// La contraseña llega codificada en Base64 (no en texto plano) — ver EncryptFacade/DecryptFacade
public record RegisterRequest(

    @NotBlank
    @Email
    String email,

    @NotBlank
    String password
) {

}
