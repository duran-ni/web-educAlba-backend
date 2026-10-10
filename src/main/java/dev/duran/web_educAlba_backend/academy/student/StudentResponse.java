package dev.duran.web_educAlba_backend.academy;

// Datos de salida al consultar un alumno
public record StudentResponse(
    Long id,
    String firstName,
    String lastName,
    String serviceOfInterest,
    Integer age,
    String phone,
    EducationalStage educationalStage,
    StudentStatus status
) {

}
