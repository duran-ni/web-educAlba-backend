package dev.duran.web_educAlba_backend.academy.dashboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

// Mantiene abiertas las conexiones SSE (Server-Sent Events) del Dashboard
// del Administrador y avisa a todas ellas cuando cambia algo que afecta al
// resumen (crear/eliminar un taller, inscribir un alumno)
@Service
public class DashboardEventsService {

    // Tiempo maximo que se mantiene abierta cada conexion antes de que el
    // propio Spring la cierre; el frontend (EventSource) reconecta solo
    private static final long EMITTER_TIMEOUT_MS = 30 * 60 * 1000L; // 30 minutos

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong();

    // Registra una nueva conexion y la devuelve para que el controlador se
    // la entregue al navegador que se acaba de suscribir
    public SseEmitter subscribe() {
        Long id = nextId.incrementAndGet();
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);

        emitter.onCompletion(() -> emitters.remove(id));
        emitter.onTimeout(() -> emitters.remove(id));
        emitter.onError(ex -> emitters.remove(id));

        emitters.put(id, emitter);
        return emitter;
    }

    // Avisa a todos los clientes conectados de que el resumen ha cambiado.
    // No se manda el resumen dentro del evento: cada cliente, al recibirlo,
    // vuelve a pedirlo con una peticion normal (mas sencillo que mantener
    // sincronizado el contenido de cada evento)
    public void notifyChange() {
        List<Long> deadEmitters = new ArrayList<>();

        emitters.forEach((id, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name("summary-changed").data("changed"));
            } catch (Exception ex) {
                deadEmitters.add(id);
            }
        });

        deadEmitters.forEach(emitters::remove);
    }
}
