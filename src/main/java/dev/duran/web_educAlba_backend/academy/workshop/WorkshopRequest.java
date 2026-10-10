package dev.duran.web_educAlba_backend.academy;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Datos de entrada para crear o actualizar un taller
public record WorkshopRequest(

    @NotBlank
    String name,

    String description,

    @NotNull
    LocalDate date,

    @NotNull
    LocalTime time,

    String recommendedAge,

    String room,

    @NotNull
    Boolean active
) {

}
