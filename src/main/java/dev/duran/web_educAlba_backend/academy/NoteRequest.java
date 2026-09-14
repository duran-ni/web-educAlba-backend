package dev.duran.web_educAlba_backend.academy;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Datos de entrada para crear o actualizar una nota
// El autor no viaja en el DTO: se toma del usuario autenticado (ver NoteController)
public record NoteRequest(

    @NotBlank
    String content,

    @NotNull
    LocalDate date,

    @NotNull
    Boolean pinned,

    @NotNull
    Long studentId
) {

}
