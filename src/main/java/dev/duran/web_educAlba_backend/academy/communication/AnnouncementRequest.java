package dev.duran.web_educAlba_backend.academy;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Datos de entrada para crear o actualizar un aviso
// El autor no viaja en el DTO: se toma del usuario autenticado (ver AnnouncementController)
public record AnnouncementRequest(

    @NotBlank
    String title,

    @NotBlank
    String content,

    @NotNull
    LocalDate date
) {

}
