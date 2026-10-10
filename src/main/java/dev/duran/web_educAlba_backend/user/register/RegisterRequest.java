package dev.duran.web_educAlba_backend.user;

import dev.duran.web_educAlba_backend.academy.student.EducationalStage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Datos que llegan del formulario de registro del frontend
// La contraseña llega codificada en Base64 (no en texto plano) — ver EncryptFacade/DecryptFacade
public record RegisterRequest(

    @NotBlank
    String firstName,

    @NotBlank
    String lastName,

    @NotBlank
    @Email
    String email,

    @NotBlank
    String password,

    @NotBlank
    String serviceOfInterest,

    @NotNull
    EducationalStage educationalStage
) {

}
