package dev.duran.web_educAlba_backend.academy.teacher;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TeacherNotFoundException extends RuntimeException {
    public TeacherNotFoundException(Long id) {
        super("No existe ningun profesor con id " + id);
    }
}
