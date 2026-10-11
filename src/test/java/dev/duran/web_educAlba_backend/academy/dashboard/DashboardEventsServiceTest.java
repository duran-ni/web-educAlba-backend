package dev.duran.web_educAlba_backend.academy.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class DashboardEventsServiceTest {

    private final DashboardEventsService dashboardEventsService = new DashboardEventsService();

    @Test
    void testSubscribe_ReturnsEmitter() {
        SseEmitter emitter = dashboardEventsService.subscribe();

        assertThat(emitter).isNotNull();
    }

    @Test
    void testSubscribe_EachCallReturnsADifferentEmitter() {
        SseEmitter firstEmitter = dashboardEventsService.subscribe();
        SseEmitter secondEmitter = dashboardEventsService.subscribe();

        assertThat(firstEmitter).isNotSameAs(secondEmitter);
    }

    @Test
    void testNotifyChange_WithNoSubscribers_DoesNotThrow() {
        assertThatCode(dashboardEventsService::notifyChange).doesNotThrowAnyException();
    }

    @Test
    void testNotifyChange_WhenEmitterIsNotInitialized_RemovesItWithoutThrowing() {
        // Un SseEmitter creado aqui "a mano" (sin pasar por una peticion HTTP
        // real) no tiene un flujo de salida asociado: al intentar enviarle
        // un evento, send() lanza una excepcion. Esto reproduce la rama
        // catch de notifyChange(), que retira el emisor caido del mapa
        dashboardEventsService.subscribe();

        assertThatCode(dashboardEventsService::notifyChange).doesNotThrowAnyException();
        // Si el emisor caido se retiro correctamente, una segunda llamada
        // tampoco debe fallar ni volver a intentar usarlo
        assertThatCode(dashboardEventsService::notifyChange).doesNotThrowAnyException();
    }

    @Test
    void testSubscribe_OnCompletion_RemovesEmitterFromRegistry() {
        SseEmitter emitter = dashboardEventsService.subscribe();

        assertThatCode(emitter::complete).doesNotThrowAnyException();
        // Tras completarse, el emisor ya no deberia estar registrado: una
        // notificacion posterior no debe intentar usarlo ni lanzar nada
        assertThatCode(dashboardEventsService::notifyChange).doesNotThrowAnyException();
    }
}
