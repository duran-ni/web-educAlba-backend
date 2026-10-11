package dev.duran.web_educAlba_backend.user.passwordreset;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// Borra un token de restablecimiento en su propia transaccion independiente.
// Existe por una razon muy concreta: si el borrado se hiciera dentro de la
// misma transaccion que PasswordResetService.resetPassword(), y ese metodo
// termina lanzando una excepcion justo despues (token caducado), Spring
// revertiria tambien el borrado junto con el resto de la transaccion
@Service
public class PasswordResetTokenCleanupService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public PasswordResetTokenCleanupService(PasswordResetTokenRepository passwordResetTokenRepository) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteExpiredToken(PasswordResetToken token) {
        passwordResetTokenRepository.delete(token);
    }
}
