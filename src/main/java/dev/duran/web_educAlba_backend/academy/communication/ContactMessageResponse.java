package dev.duran.web_educAlba_backend.academy.communication;

import java.time.LocalDateTime;

// Datos de salida tras registrar un mensaje de contacto

public record ContactMessageResponse(
    Long id,
    String name,
    String email,
    String subject,
    String message,
    LocalDateTime sentAt
) {
    
}
