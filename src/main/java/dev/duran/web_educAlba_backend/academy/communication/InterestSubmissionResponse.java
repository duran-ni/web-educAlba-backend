package dev.duran.web_educAlba_backend.academy;

import java.time.LocalDateTime;

// Datos de salida tras registrar una solicitud de interés
public record InterestSubmissionResponse(
    Long id,
    String studentName,
    String courseToReinforce,
    LocalDateTime submittedAt
) {

}
