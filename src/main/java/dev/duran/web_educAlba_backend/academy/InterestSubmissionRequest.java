package dev.duran.web_educAlba_backend.academy;

import jakarta.validation.constraints.NotBlank;

// Datos de entrada del formulario rápido de interés en Inicio
public record InterestSubmissionRequest(

    @NotBlank
    String studentName,

    @NotBlank
    String courseToReinforce
) {

}
