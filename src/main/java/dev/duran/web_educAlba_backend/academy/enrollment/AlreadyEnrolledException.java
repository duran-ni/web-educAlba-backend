package dev.duran.web_educAlba_backend.academy.enrollment;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AlreadyEnrolledException extends RuntimeException {
    public AlreadyEnrolledException(Long workshopId) {
        super("Ya estas inscrito en el taller con id " + workshopId);
    }
}
