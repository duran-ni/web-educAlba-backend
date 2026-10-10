package dev.duran.web_educAlba_backend.user.passwordreset;

import dev.duran.web_educAlba_backend.user.core.UserEntity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    // Necesario para localizar la solicitud de restablecimiento a partir
    // del token que llega en el enlace del correo
    Optional<PasswordResetToken> findByToken(String token);

    // Necesario para invalidar cualquier token anterior del mismo usuario
    // al generar uno nuevo, de forma que solo el enlace mas reciente funcione
    Optional<PasswordResetToken> findByUser(UserEntity user);
}
