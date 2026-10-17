package dev.duran.web_educAlba_backend.academy.teacher;

// Datos de salida al consultar un profesor
public record TeacherResponse(
    Long id,
    String name,
    String specialty
) {
}
