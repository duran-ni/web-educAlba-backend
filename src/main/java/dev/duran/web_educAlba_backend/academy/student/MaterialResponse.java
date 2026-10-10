package dev.duran.web_educAlba_backend.academy;

// Datos de salida al consultar un material
public record MaterialResponse(
    Long id,
    String name,
    String subject,
    Long fileSize,
    String filePath,
    Long studentId,
    String studentFullName
) {

}
