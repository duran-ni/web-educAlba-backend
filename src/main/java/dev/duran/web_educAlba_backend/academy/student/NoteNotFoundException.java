package dev.duran.web_educAlba_backend.academy;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoteNotFoundException extends RuntimeException {
    public NoteNotFoundException(Long id) {
        super("No existe ninguna nota con id " + id);
    }
}
