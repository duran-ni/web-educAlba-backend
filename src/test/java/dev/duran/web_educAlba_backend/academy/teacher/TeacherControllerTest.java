package dev.duran.web_educAlba_backend.academy.teacher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;

import dev.duran.web_educAlba_backend.TestcontainersConfiguration;
import tools.jackson.databind.ObjectMapper;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TeacherControllerTest {

    private static final String ADMIN_EMAIL = "admin@educalba.com";
    private static final String ADMIN_PASSWORD = "AdminEducAlba123";

    @Autowired
    WebApplicationContext context;

    @Autowired
    ObjectMapper mapper;

    // Sustituye al bean real de Cloudinary por un doble de prueba, para no
    // subir ni borrar imágenes de verdad en la cuenta real al ejecutar los tests
    @MockitoBean
    Cloudinary cloudinary;

    Uploader uploader;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        // cloudinary.uploader() es quien hace la subida/borrado real, asi que
        // también lo mockeamos para controlar lo que "responde" Cloudinary
        uploader = mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);
    }

    private Long createTeacher(String name, String specialty) throws Exception {
        TeacherRequest request = new TeacherRequest(name, specialty);

        String body = mockMvc.perform(post("/api/admin/teachers")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return mapper.readValue(body, TeacherResponse.class).id();
    }

    @Test
    @DisplayName("A new teacher can be created")
    void testCreate_ReturnsCreated() throws Exception {
        TeacherRequest request = new TeacherRequest("Laura Gómez", "Matemáticas");

        mockMvc.perform(post("/api/admin/teachers")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Laura Gómez"))
                .andExpect(jsonPath("$.specialty").value("Matemáticas"))
                .andExpect(jsonPath("$.photoUrl").doesNotExist());
    }

    @Test
    @DisplayName("All teachers can be listed")
    void testGetAll_ReturnsOk() throws Exception {
        TeacherRequest request = new TeacherRequest("Marcos Ruiz", "Educación Física");

        mockMvc.perform(post("/api/admin/teachers")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/admin/teachers")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("A single teacher can be retrieved by id")
    void testGetById_ReturnsOk() throws Exception {
        TeacherRequest createRequest = new TeacherRequest("Ana Torres", "Inglés");

        String createBody = mockMvc.perform(post("/api/admin/teachers")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        TeacherResponse created = mapper.readValue(createBody, TeacherResponse.class);

        mockMvc.perform(get("/api/admin/teachers/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana Torres"));
    }

    @Test
    @DisplayName("Retrieving a teacher that does not exist returns Not Found")
    void testGetById_NonExistentTeacher_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/admin/teachers/999999")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("An existing teacher can be updated")
    void testUpdate_ReturnsOk() throws Exception {
        TeacherRequest createRequest = new TeacherRequest("Pedro Sánchez", "Historia");

        String createBody = mockMvc.perform(post("/api/admin/teachers")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        TeacherResponse created = mapper.readValue(createBody, TeacherResponse.class);
        TeacherRequest updateRequest = new TeacherRequest("Pedro Sánchez", "Geografía");

        mockMvc.perform(put("/api/admin/teachers/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialty").value("Geografía"));
    }

    @Test
    @DisplayName("Updating a teacher that does not exist returns Not Found")
    void testUpdate_NonExistentTeacher_ReturnsNotFound() throws Exception {
        TeacherRequest updateRequest = new TeacherRequest("No importa", "No importa");

        mockMvc.perform(put("/api/admin/teachers/999999")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("A photo can be uploaded for an existing teacher")
    void testUploadPhoto_ReturnsOkWithPhotoUrl() throws Exception {
        Long teacherId = createTeacher("Elena Vidal", "Dibujo");

        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(Map.of(
                "secure_url", "https://res.cloudinary.com/demo/elena.jpg",
                "public_id", "elena-public-id"));

        MockMultipartFile image = new MockMultipartFile(
                "image", "photo.jpg", "image/jpeg", "fake-image-content".getBytes());

        mockMvc.perform(multipart("/api/admin/teachers/" + teacherId + "/photo")
                .file(image)
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.photoUrl").value("https://res.cloudinary.com/demo/elena.jpg"));
    }

    @Test
    @DisplayName("Uploading a photo for a teacher that does not exist returns Not Found")
    void testUploadPhoto_NonExistentTeacher_ReturnsNotFound() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "photo.jpg", "image/jpeg", "fake-image-content".getBytes());

        mockMvc.perform(multipart("/api/admin/teachers/999999/photo")
                .file(image)
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("A Cloudinary failure during photo upload returns Bad Gateway")
    void testUploadPhoto_CloudinaryFailure_ReturnsBadGateway() throws Exception {
        Long teacherId = createTeacher("Iván Castro", "Tecnología");

        when(uploader.upload(any(byte[].class), anyMap()))
                .thenThrow(new IOException("Cloudinary no responde"));

        MockMultipartFile image = new MockMultipartFile(
                "image", "photo.jpg", "image/jpeg", "fake-image-content".getBytes());

        mockMvc.perform(multipart("/api/admin/teachers/" + teacherId + "/photo")
                .file(image)
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isBadGateway());
    }

    @Test
    @DisplayName("A deleted teacher with a photo also removes it from Cloudinary")
    void testDelete_TeacherWithPhoto_DestroysCloudinaryPhoto() throws Exception {
        Long teacherId = createTeacher("Sofía Díaz", "Música");

        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(Map.of(
                "secure_url", "https://res.cloudinary.com/demo/sofia.jpg",
                "public_id", "sofia-public-id"));

        MockMultipartFile image = new MockMultipartFile(
                "image", "photo.jpg", "image/jpeg", "fake-image-content".getBytes());

        mockMvc.perform(multipart("/api/admin/teachers/" + teacherId + "/photo")
                .file(image)
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isOk());

        when(uploader.destroy(anyString(), anyMap())).thenReturn(Map.of("result", "ok"));

        mockMvc.perform(delete("/api/admin/teachers/" + teacherId)
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/admin/teachers/" + teacherId)
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("A deleted teacher can no longer be found")
    void testDelete_ThenGetReturnsNotFound() throws Exception {
        TeacherRequest createRequest = new TeacherRequest("Sofía Díaz", "Música");

        String createBody = mockMvc.perform(post("/api/admin/teachers")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        TeacherResponse created = mapper.readValue(createBody, TeacherResponse.class);

        mockMvc.perform(delete("/api/admin/teachers/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/admin/teachers/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deleting a teacher that does not exist returns Not Found")
    void testDelete_NonExistentTeacher_ReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/admin/teachers/999999")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Creating a teacher with a missing required field is rejected")
    void testCreate_MissingRequiredField_ReturnsBadRequest() throws Exception {
        String invalidJson = "{\"specialty\": \"Falta el nombre\"}";

        mockMvc.perform(post("/api/admin/teachers")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Accessing the admin teachers endpoint without authentication is rejected")
    void testGetAll_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/teachers"))
                .andExpect(status().isUnauthorized());
    }
}
