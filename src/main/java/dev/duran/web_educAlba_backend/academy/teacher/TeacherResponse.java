package dev.duran.web_educAlba_backend.academy.teacher;

// DTO de salida: lo que el backend devuelve al frontend al consultar
// un profesor (tanto en el listado público como en el panel de admin)
public record TeacherResponse(
        Long id,
        String name,
        String specialty,
        String photoUrl
) {
}
