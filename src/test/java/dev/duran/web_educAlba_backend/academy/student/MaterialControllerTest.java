package dev.duran.web_educAlba_backend.academy.student;

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
class MaterialControllerTest {

    private static final String ADMIN_EMAIL = "admin@educalba.com";
    private static final String ADMIN_PASSWORD = "AdminEducAlba123";

    @Autowired
    WebApplicationContext context;

    @Autowired
    ObjectMapper mapper;

    // Sustituye al bean real de Cloudinary por un doble de prueba, para no
    // subir ni borrar archivos de verdad en la cuenta real al ejecutar los tests
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
        // tambien lo mockeamos para controlar lo que "responde" Cloudinary
        uploader = mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);
    }

    private Long createStudent() throws Exception {
        StudentRequest request = new StudentRequest("Carlos", "Diaz", "Refuerzo", EducationalStage.ESO);

        String body = mockMvc.perform(post("/api/admin/students")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return mapper.readValue(body, StudentResponse.class).id();
    }

    private MockMultipartFile samplePdf() {
        return new MockMultipartFile(
                "file", "worksheet.pdf", "application/pdf", "fake-pdf-content".getBytes());
    }

    @Test
    @DisplayName("A new material can be uploaded")
    void testUpload_ReturnsCreated() throws Exception {
        when(uploader.upload(any(byte[].class), anyMap()))
                .thenReturn(Map.of(
                        "secure_url", "https://res.cloudinary.com/demo/raw/upload/worksheet.pdf",
                        "public_id", "worksheet-public-id"));

        Long studentId = createStudent();

        mockMvc.perform(multipart("/api/admin/materials")
                .file(samplePdf())
                .param("title", "Worksheet 1")
                .param("description", "Resuelve los ejercicios 1 y 2")
                .param("studentId", String.valueOf(studentId))
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Worksheet 1"))
                .andExpect(jsonPath("$.fileUrl").value("https://res.cloudinary.com/demo/raw/upload/worksheet.pdf"))
                .andExpect(jsonPath("$.downloadUrl").value(
                        "https://res.cloudinary.com/demo/raw/upload/fl_attachment:Worksheet_1/worksheet.pdf"))
                .andExpect(jsonPath("$.studentFullName").value("Carlos Diaz"));
    }

    @Test
    @DisplayName("A material's download URL slugifies accents, spaces and symbols in the title")
    void testUpload_DownloadUrl_SlugifiesAccentsAndSymbols() throws Exception {
        when(uploader.upload(any(byte[].class), anyMap()))
                .thenReturn(Map.of(
                        "secure_url", "https://res.cloudinary.com/demo/raw/upload/accented.pdf",
                        "public_id", "accented-public-id"));

        Long studentId = createStudent();

        mockMvc.perform(multipart("/api/admin/materials")
                .file(samplePdf())
                .param("title", "Tarea de Matemáticas: página 5")
                .param("studentId", String.valueOf(studentId))
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.downloadUrl").value(
                        "https://res.cloudinary.com/demo/raw/upload/fl_attachment:Tarea_de_Matematicas_pagina_5/accented.pdf"));
    }

    @Test
    @DisplayName("All materials can be listed")
    void testGetAll_ReturnsOk() throws Exception {
        when(uploader.upload(any(byte[].class), anyMap()))
                .thenReturn(Map.of(
                        "secure_url", "https://res.cloudinary.com/demo/raw/upload/reading-guide.pdf",
                        "public_id", "reading-guide-public-id"));

        Long studentId = createStudent();

        mockMvc.perform(multipart("/api/admin/materials")
                .file(samplePdf())
                .param("title", "Reading guide")
                .param("studentId", String.valueOf(studentId))
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/admin/materials")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("An existing material can be retrieved by id")
    void testGetById_ReturnsOk() throws Exception {
        when(uploader.upload(any(byte[].class), anyMap()))
                .thenReturn(Map.of(
                        "secure_url", "https://res.cloudinary.com/demo/raw/upload/retrieve.pdf",
                        "public_id", "retrieve-public-id"));

        Long studentId = createStudent();

        String createBody = mockMvc.perform(multipart("/api/admin/materials")
                .file(samplePdf())
                .param("title", "Material to retrieve")
                .param("studentId", String.valueOf(studentId))
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        MaterialResponse created = mapper.readValue(createBody, MaterialResponse.class);

        mockMvc.perform(get("/api/admin/materials/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Material to retrieve"));
    }

    @Test
    @DisplayName("Uploading a material for a non-existent student is rejected")
    void testUpload_UnknownStudent_ReturnsNotFound() throws Exception {
        mockMvc.perform(multipart("/api/admin/materials")
                .file(samplePdf())
                .param("title", "Orphan material")
                .param("studentId", "999999")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("A material's title and description can be edited")
    void testUpdateDetails_ReturnsOkWithNewTitle() throws Exception {
        when(uploader.upload(any(byte[].class), anyMap()))
                .thenReturn(Map.of(
                        "secure_url", "https://res.cloudinary.com/demo/raw/upload/typo.pdf",
                        "public_id", "typo-public-id"));

        Long studentId = createStudent();

        String createBody = mockMvc.perform(multipart("/api/admin/materials")
                .file(samplePdf())
                .param("title", "Tarea de Lngua")
                .param("studentId", String.valueOf(studentId))
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        MaterialResponse created = mapper.readValue(createBody, MaterialResponse.class);

        mockMvc.perform(put("/api/admin/materials/" + created.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\": \"Tarea de Lengua\", \"description\": \"Ejercicios 1 y 2\"}")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Tarea de Lengua"))
                .andExpect(jsonPath("$.description").value("Ejercicios 1 y 2"));
    }

    @Test
    @DisplayName("Editing a material that does not exist returns Not Found")
    void testUpdateDetails_NonExistentMaterial_ReturnsNotFound() throws Exception {
        mockMvc.perform(put("/api/admin/materials/999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\": \"No importa\"}")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("A deleted material can no longer be found")
    void testDelete_ThenGetReturnsNotFound() throws Exception {
        when(uploader.upload(any(byte[].class), anyMap()))
                .thenReturn(Map.of(
                        "secure_url", "https://res.cloudinary.com/demo/raw/upload/temp.pdf",
                        "public_id", "temp-public-id"));

        Long studentId = createStudent();

        String createBody = mockMvc.perform(multipart("/api/admin/materials")
                .file(samplePdf())
                .param("title", "Temporary file")
                .param("studentId", String.valueOf(studentId))
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        MaterialResponse created = mapper.readValue(createBody, MaterialResponse.class);

        when(uploader.destroy(anyString(), anyMap())).thenReturn(Map.of("result", "ok"));

        mockMvc.perform(delete("/api/admin/materials/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/admin/materials/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Uploading without a title is rejected")
    void testUpload_MissingTitle_ReturnsBadRequest() throws Exception {
        Long studentId = createStudent();

        mockMvc.perform(multipart("/api/admin/materials")
                .file(samplePdf())
                .param("studentId", String.valueOf(studentId))
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("A Cloudinary failure during upload returns Bad Gateway")
    void testUpload_CloudinaryFailure_ReturnsBadGateway() throws Exception {
        when(uploader.upload(any(byte[].class), anyMap()))
                .thenThrow(new IOException("Cloudinary no responde"));

        Long studentId = createStudent();

        mockMvc.perform(multipart("/api/admin/materials")
                .file(samplePdf())
                .param("title", "Material que falla")
                .param("studentId", String.valueOf(studentId))
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isBadGateway());
    }

    @Test
    @DisplayName("A Cloudinary failure during delete returns Bad Gateway")
    void testDelete_CloudinaryFailure_ReturnsBadGateway() throws Exception {
        when(uploader.upload(any(byte[].class), anyMap()))
                .thenReturn(Map.of(
                        "secure_url", "https://res.cloudinary.com/demo/raw/upload/delete-failure.pdf",
                        "public_id", "delete-failure-public-id"));

        Long studentId = createStudent();

        String createBody = mockMvc.perform(multipart("/api/admin/materials")
                .file(samplePdf())
                .param("title", "Material que falla al borrar")
                .param("studentId", String.valueOf(studentId))
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        MaterialResponse created = mapper.readValue(createBody, MaterialResponse.class);

        when(uploader.destroy(anyString(), anyMap()))
                .thenThrow(new IOException("Cloudinary no responde"));

        mockMvc.perform(delete("/api/admin/materials/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isBadGateway());
    }
}
