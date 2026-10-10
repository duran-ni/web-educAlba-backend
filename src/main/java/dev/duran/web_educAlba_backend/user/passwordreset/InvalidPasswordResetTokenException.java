package dev.duran.web_educAlba_backend.user.passwordreset;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Se lanza cuando el token de restablecimiento no existe o ya ha caducado
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPasswordResetTokenException extends RuntimeException {

    public InvalidPasswordResetTokenException(String message) {
        super(message);
    }
}
