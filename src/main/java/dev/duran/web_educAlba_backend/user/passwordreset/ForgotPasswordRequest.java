package dev.duran.web_educAlba_backend.user.passwordreset;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Datos que llegan del formulario de "Olvide mi contrasena"
public record ForgotPasswordRequest(

    @NotBlank
    @Email
    String email
) {

}
