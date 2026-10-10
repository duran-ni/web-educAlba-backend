package dev.duran.web_educAlba_backend.academy;

import java.time.LocalDate;

// Datos de salida al consultar una inscripcion
public record EnrollmentResponse(
    Long id,
    Long studentId,
    String studentFullName,
    Long workshopId,
    String workshopName,
    LocalDate enrollmentDate,
    String progress
) {

}
