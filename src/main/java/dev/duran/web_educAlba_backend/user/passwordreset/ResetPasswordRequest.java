package dev.duran.web_educAlba_backend.user.passwordreset;

import jakarta.validation.constraints.NotBlank;

// Datos que llegan del formulario de restablecimiento de contraseña
// La contraseña llega codificada en Base64 (no en texto plano) — ver EncryptFacade/DecryptFacade
public record ResetPasswordRequest(

    @NotBlank
    String token,

    @NotBlank
    String password
) {

}
