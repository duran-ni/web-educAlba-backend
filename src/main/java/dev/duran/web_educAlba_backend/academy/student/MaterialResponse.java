package dev.duran.web_educAlba_backend.academy.student;

import java.time.LocalDateTime;

// Datos de salida al consultar un material.
// No se expone publicId: es un dato interno, solo lo necesita el backend
// para comunicarse con Cloudinary
public record MaterialResponse(
    Long id,
    String title,
    String description,
    String fileUrl,
    LocalDateTime uploadedAt,
    Long studentId,
    String studentFullName
) {

}
