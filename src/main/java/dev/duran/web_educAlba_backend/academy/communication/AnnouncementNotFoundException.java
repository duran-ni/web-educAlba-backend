package dev.duran.web_educAlba_backend.academy;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AnnouncementNotFoundException extends RuntimeException {
    public AnnouncementNotFoundException(Long id) {
        super("No existe ningun aviso con id " + id);
    }
}
