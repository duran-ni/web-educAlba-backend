package dev.duran.web_educAlba_backend.academy.teacher;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import dev.duran.web_educAlba_backend.TestcontainersConfiguration;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PublicTeacherControllerTest {

    @Autowired
    WebApplicationContext context;

    @Autowired
    TeacherRepository teacherRepository;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        // Limpia los profesores que hayan dejado otras clases de test, para
        // que el escenario "sin profesores" sea fiable
        teacherRepository.deleteAll();
    }

    @Test
    @DisplayName("A visitor can list all teachers without authentication")
    void testGetAll_ReturnsOk() throws Exception {
        teacherRepository.save(Teacher.builder()
                .name("Laura Gómez")
                .specialty("Matemáticas")
                .build());

        mockMvc.perform(get("/api/public/teachers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Laura Gómez"))
                .andExpect(jsonPath("$[0].specialty").value("Matemáticas"));
    }

    @Test
    @DisplayName("An empty list is returned when there are no teachers")
    void testGetAll_NoTeachers_ReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/public/teachers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
