package dev.duran.web_educAlba_backend.academy;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

// Datos de entrada del formulario público "¡Apúntate ya!"
public record PublicEnrollmentRequest(

    @NotBlank
    String studentName,

    @NotNull
    @Positive
    Integer age,

    @NotBlank
    @Pattern(regexp = "^[6789][0-9]{8}$", message = "El teléfono debe tener 9 dígitos y empezar por 6, 7, 8 o 9")
    String phone,

    @NotEmpty
    List<Long> workshopIds
) {

}
