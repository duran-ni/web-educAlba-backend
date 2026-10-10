package dev.duran.web_educAlba_backend.academy;

import java.time.LocalDate;

// Datos de salida al consultar un aviso
public record AnnouncementResponse(
    Long id,
    String title,
    String content,
    LocalDate date,
    Long authorId,
    String authorEmail
) {

}
