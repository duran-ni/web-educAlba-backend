package dev.duran.web_educAlba_backend.academy.dashboard;

// Datos de salida para el resumen (KPIs) del Dashboard Administrador
public record DashboardSummaryResponse(

    long totalEnrolledStudents,

    long totalActiveWorkshops
) {

}
