package dev.duran.web_educAlba_backend.academy;

import java.time.LocalDate;
import java.time.LocalTime;

// Datos de salida al consultar un evento de agenda
// workshopId y workshopName pueden ser null si el evento no pertenece a ningun taller
public record AgendaEventResponse(
    Long id,
    LocalDate date,
    LocalTime time,
    String activity,
    String room,
    Long workshopId,
    String workshopName
) {

}
