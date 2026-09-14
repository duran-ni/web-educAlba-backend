package dev.duran.web_educAlba_backend.exception;

import java.time.LocalDateTime;

// Estructura única para cualquier respuesta de error de la API
public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path
){

}
