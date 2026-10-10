package dev.duran.web_educAlba_backend.user.passwordreset;

import dev.duran.web_educAlba_backend.user.register.InvalidPasswordException;

import dev.duran.web_educAlba_backend.user.core.UserEntity;
import dev.duran.web_educAlba_backend.user.core.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.duran.web_educAlba_backend.facade.decrypt.IDecryptFacade;
import dev.duran.web_educAlba_backend.facade.encrypt.IEncryptFacade;

@Service
public class PasswordResetService {

    // El token caduca una hora despues de generarse
    private static final long TOKEN_VALIDITY_HOURS = 1;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final IDecryptFacade decryptFacade;
    private final IEncryptFacade encryptFacade;
    private final JavaMailSender javaMailSender;
    private final String fromAddress;
    private final String frontendBaseUrl;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            IDecryptFacade decryptFacade,
            IEncryptFacade encryptFacade,
            JavaMailSender javaMailSender,
            @Value("${app.mail.from-address}") String fromAddress,
            @Value("${app.frontend.base-url}") String frontendBaseUrl) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.decryptFacade = decryptFacade;
        this.encryptFacade = encryptFacade;
        this.javaMailSender = javaMailSender;
        this.fromAddress = fromAddress;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    // Si el email existe, genera un token nuevo y envia el correo de recuperacion.
    // Si no existe, no hace nada: el metodo nunca informa si el email esta
    // registrado o no (ver PasswordResetController), para evitar que el
    // formulario pueda usarse para averiguar que emails existen en el sistema
    @Transactional
    public void requestReset(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            // Invalida cualquier token anterior del mismo usuario: solo el
            // enlace del correo mas reciente debe funcionar
            passwordResetTokenRepository.findByUser(user)
                .ifPresent(passwordResetTokenRepository::delete);

            PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(TOKEN_VALIDITY_HOURS))
                .build();

            passwordResetTokenRepository.save(passwordResetToken);
            sendResetEmail(user, passwordResetToken.getToken());
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(request.token())
            .orElseThrow(() -> new InvalidPasswordResetTokenException("El enlace de restablecimiento no es válido"));

        if (passwordResetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(passwordResetToken);
            throw new InvalidPasswordResetTokenException("El enlace de restablecimiento ha caducado");
        }

        String decodedPassword = decryptFacade.decode("base64", request.password());

        if (decodedPassword.length() < 8) {
            throw new InvalidPasswordException("La contraseña debe tener al menos 8 caracteres");
        }

        UserEntity user = passwordResetToken.getUser();
        user.setPassword(encryptFacade.encode("bcrypt", decodedPassword));
        userRepository.save(user);

        // Token de un solo uso: se elimina en cuanto se ha consumido
        passwordResetTokenRepository.delete(passwordResetToken);
    }

    // Construye y envia el correo con el enlace de restablecimiento
    private void sendResetEmail(UserEntity user, String token) {
        String resetLink = frontendBaseUrl + "/restablecer-contrasena?token=" + token;

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(fromAddress);
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject("Restablece tu contraseña de EducAlba");
        mailMessage.setText(
            "Hemos recibido una solicitud para restablecer tu contraseña.\n\n"
                + "Pulsa este enlace para crear una nueva contraseña (válido durante 1 hora):\n"
                + resetLink + "\n\n"
                + "Si no has solicitado este cambio, puedes ignorar este correo.");

        javaMailSender.send(mailMessage);
    }
}
