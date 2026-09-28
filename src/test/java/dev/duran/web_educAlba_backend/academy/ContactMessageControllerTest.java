package dev.duran.web_educAlba_backend.academy;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import tools.jackson.databind.ObjectMapper;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ContactMessageControllerTest {

    @Autowired
    WebApplicationContext context;

    @Autowired
    ObjectMapper mapper;

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

    @Test
    @DisplayName("A visitor can submit the contact form without authentication")
    void testCreate_ReturnsCreated() throws Exception {
        ContactMessageRequest request = new ContactMessageRequest(
            "Ana García", "ana@example.com", "Solicitud de tutoría personalizada", "Hola, me gustaría más información.");

        mockMvc.perform(post("/api/public/contact-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Ana García"))
            .andExpect(jsonPath("$.email").value("ana@example.com"))
            .andExpect(jsonPath("$.subject").value("Solicitud de tutoría personalizada"));

        verify(javaMailSender).send((SimpleMailMessage) org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Submitting the form with a missing required field is rejected")
    void testCreate_MissingRequiredField_ReturnsBadRequest() throws Exception {
        String invalidJson = "{\"email\": \"ana@example.com\", \"subject\": \"Consulta\", \"message\": \"Hola\"}";

        mockMvc.perform(post("/api/public/contact-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Submitting the form with an invalid email format is rejected")
    void testCreate_InvalidEmail_ReturnsBadRequest() throws Exception {
        ContactMessageRequest request = new ContactMessageRequest(
            "Ana García", "not-an-email", "Consulta", "Hola, me gustaría más información.");

        mockMvc.perform(post("/api/public/contact-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}
