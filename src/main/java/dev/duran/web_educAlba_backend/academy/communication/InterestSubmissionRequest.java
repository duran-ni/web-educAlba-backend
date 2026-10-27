package dev.duran.web_educAlba_backend.academy.communication;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// Datos de entrada del formulario rápido de interés en Inicio
public record InterestSubmissionRequest(

    @NotBlank
    String studentName,

    @NotBlank
    String courseToReinforce,

    @NotBlank
    @Pattern(regexp = "^[6789][0-9]{8}$", message = "El teléfono debe tener 9 dígitos y empezar por 6, 7, 8 o 9")
    String phone
) {

}
