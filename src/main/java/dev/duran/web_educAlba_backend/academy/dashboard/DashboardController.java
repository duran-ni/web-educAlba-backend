package dev.duran.web_educAlba_backend.academy.dashboard;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final DashboardEventsService dashboardEventsService;

    public DashboardController(DashboardService dashboardService, DashboardEventsService dashboardEventsService) {
        this.dashboardService = dashboardService;
        this.dashboardEventsService = dashboardEventsService;
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse getSummary() {
        return dashboardService.getSummary();
    }

    // Conexion persistente: el frontend se suscribe una sola vez y recibe
    // un aviso cada vez que cambia algo que afecta al resumen, en vez de
    // tener que preguntar por sondeo (polling) cada X segundos
    @GetMapping("/summary/stream")
    public SseEmitter streamSummary() {
        return dashboardEventsService.subscribe();
    }
}
