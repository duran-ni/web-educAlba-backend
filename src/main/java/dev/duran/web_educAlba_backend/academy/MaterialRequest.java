package dev.duran.web_educAlba_backend.academy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Datos de entrada para crear o actualizar un material
public record MaterialRequest(

    @NotBlank
    String name,

    String subject,

    Long fileSize,

    String filePath,

    @NotNull
    Long studentId
) {

}
