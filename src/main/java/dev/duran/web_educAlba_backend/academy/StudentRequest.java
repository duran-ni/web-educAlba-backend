package dev.duran.web_educAlba_backend.academy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Datos de entrada para crear o actualizar un alumno
public record StudentRequest(

    @NotBlank
    String firstName,

    @NotBlank
    String lastName,

    String serviceOfInterest,

    @NotNull
    EducationalStage educationalStage
) {

}
