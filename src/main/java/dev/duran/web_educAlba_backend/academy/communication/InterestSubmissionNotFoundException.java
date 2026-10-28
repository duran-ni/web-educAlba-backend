package dev.duran.web_educAlba_backend.academy.communication;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class InterestSubmissionNotFoundException extends RuntimeException {
    public InterestSubmissionNotFoundException(Long id) {
        super("No existe ninguna solicitud de interés con id " + id);
    }
}
