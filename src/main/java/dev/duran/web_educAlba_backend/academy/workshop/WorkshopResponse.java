package dev.duran.web_educAlba_backend.academy;

import java.time.LocalDate;
import java.time.LocalTime;

// Datos de salida al consultar un taller
public record WorkshopResponse(
    Long id,
    String name,
    String description,
    LocalDate date,
    LocalTime time,
    String recommendedAge,
    String room,
    boolean active
) {

}
