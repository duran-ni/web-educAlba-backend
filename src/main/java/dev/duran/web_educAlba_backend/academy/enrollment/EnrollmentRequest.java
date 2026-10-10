package dev.duran.web_educAlba_backend.academy;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

// Datos de entrada para crear o actualizar una inscripcion
public record EnrollmentRequest(

    @NotNull
    Long studentId,

    @NotNull
    Long workshopId,

    @NotNull
    LocalDate enrollmentDate,

    String progress
) {

}
