package dev.duran.web_educAlba_backend.academy.student;

import java.time.LocalDate;
import java.time.LocalTime;

// Datos de salida de un taller en el que esta inscrito el usuario que ha
// iniciado sesion, con su progreso, para mostrarlo en su propio Dashboard
public record MyWorkshopResponse(
    Long enrollmentId,
    Long workshopId,
    String workshopName,
    LocalDate date,
    LocalTime time,
    String progress
) {

}
