package dev.duran.web_educAlba_backend.academy.workshop;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.DecimalMin;
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
    Boolean active,

    @NotNull
    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    BigDecimal price
) {

}
