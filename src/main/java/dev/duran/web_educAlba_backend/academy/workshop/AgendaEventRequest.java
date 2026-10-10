package dev.duran.web_educAlba_backend.academy;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Datos de entrada para crear o actualizar un evento de agenda
// workshopId es opcional: el evento puede no corresponder a ningun taller concreto
public record AgendaEventRequest(

    @NotNull
    LocalDate date,

    @NotNull
    LocalTime time,

    @NotBlank
    String activity,

    String room,

    Long workshopId
) {

}
