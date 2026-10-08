package dev.duran.web_educAlba_backend.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import dev.duran.web_educAlba_backend.TestcontainersConfiguration;
import dev.duran.web_educAlba_backend.academy.EducationalStage;
import tools.jackson.databind.ObjectMapper;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PasswordResetControllerTest {

    @Autowired
    WebApplicationContext context;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    // Sustituye al JavaMailSender real por un doble de prueba, para no enviar
    // emails de verdad al ejecutar los tests
    @MockitoBean
    JavaMailSender javaMailSender;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .build();
    }

    // Registra un usuario de prueba a traves del endpoint real de registro,
    // para no depender de como esta construida la entidad por dentro
    private String registerTestUser(String email) throws Exception {
        String encodedPassword = Base64.getEncoder().encodeToString("ClaveSegura123".getBytes());
        RegisterRequest request = new RegisterRequest("Lucía", "García López", email, encodedPassword,
                "Clases de Refuerzo", EducationalStage.PRIMARIA);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        return email;
    }

    @Test
    @DisplayName("Requesting a reset for a registered email sends an email and returns no content")
    void testForgotPassword_RegisteredEmail_ReturnsNoContent() throws Exception {
        String email = registerTestUser("recuperar" + System.currentTimeMillis() + "@educalba.com");
        ForgotPasswordRequest request = new ForgotPasswordRequest(email);

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());

        verify(javaMailSender).send((SimpleMailMessage) org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Requesting a reset for an unregistered email also returns no content, without sending an email")
    void testForgotPassword_UnregisteredEmail_ReturnsNoContentWithoutSendingEmail() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("noexiste@educalba.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());

        verifyNoInteractions(javaMailSender);
    }

    @Test
    @DisplayName("Resetting the password with a valid token succeeds and consumes the token")
    void testResetPassword_ValidToken_ReturnsNoContent() throws Exception {
        String email = registerTestUser("token-valido" + System.currentTimeMillis() + "@educalba.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new ForgotPasswordRequest(email))))
            .andExpect(status().isNoContent());

        UserEntity user = userRepository.findByEmail(email).orElseThrow();
        PasswordResetToken token = passwordResetTokenRepository.findByUser(user).orElseThrow();

        String encodedNewPassword = Base64.getEncoder().encodeToString("NuevaClave456".getBytes());
        ResetPasswordRequest request = new ResetPasswordRequest(token.getToken(), encodedNewPassword);

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());

        assertThat(passwordResetTokenRepository.findByToken(token.getToken())).isEmpty();
    }

    @Test
    @DisplayName("Resetting the password with a non-existent token is rejected")
    void testResetPassword_InvalidToken_ReturnsBadRequest() throws Exception {
        String encodedNewPassword = Base64.getEncoder().encodeToString("NuevaClave456".getBytes());
        ResetPasswordRequest request = new ResetPasswordRequest("token-inventado", encodedNewPassword);

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Resetting the password with a new password that is too short is rejected")
    void testResetPassword_PasswordTooShort_ReturnsBadRequest() throws Exception {
        String email = registerTestUser("clave-corta" + System.currentTimeMillis() + "@educalba.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new ForgotPasswordRequest(email))))
            .andExpect(status().isNoContent());

        UserEntity user = userRepository.findByEmail(email).orElseThrow();
        PasswordResetToken token = passwordResetTokenRepository.findByUser(user).orElseThrow();

        String encodedShortPassword = Base64.getEncoder().encodeToString("abc123".getBytes());
        ResetPasswordRequest request = new ResetPasswordRequest(token.getToken(), encodedShortPassword);

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}
