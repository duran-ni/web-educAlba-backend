package dev.duran.web_educAlba_backend.academy;

import java.time.LocalDate;

// Datos de salida al consultar una nota
public record NoteResponse(
    Long id,
    String content,
    LocalDate date,
    Boolean pinned,
    Long studentId,
    String studentFullName,
    Long authorId,
    String authorEmail
) {

}
