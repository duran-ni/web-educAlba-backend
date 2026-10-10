package dev.duran.web_educAlba_backend.academy;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AgendaEventNotFoundException extends RuntimeException {
    public AgendaEventNotFoundException(Long id) {
        super("No existe ningun evento de agenda con id " + id);
    }
}
