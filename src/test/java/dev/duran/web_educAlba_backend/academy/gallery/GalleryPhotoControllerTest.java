package dev.duran.web_educAlba_backend.academy.gallery;

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
class GalleryPhotoControllerTest {

        private static final String ADMIN_EMAIL = "admin@educalba.com";
        private static final String ADMIN_PASSWORD = "AdminEducAlba123";

        @Autowired
        WebApplicationContext context;

        @Autowired
        ObjectMapper mapper;

        // Sustituye al bean real de Cloudinary por un doble de prueba, para no
        // subir ni borrar imagenes de verdad en la cuenta real al ejecutar los tests
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

        // Crea una carpeta de prueba y devuelve su id, necesario porque ahora
        // toda foto subida debe pertenecer a una carpeta (folderId obligatorio)
        private Long createFolder() throws Exception {
                String body = mockMvc.perform(post("/api/admin/gallery/folders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\": \"Carpeta de prueba\"}")
                                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                return mapper.readValue(body, GalleryFolderResponse.class).id();
        }

        @Test
        @DisplayName("A new photo can be uploaded")
        void testUpload_ReturnsCreated() throws Exception {
                when(uploader.upload(any(byte[].class), anyMap()))
                                .thenReturn(Map.of(
                                                "secure_url", "https://res.cloudinary.com/demo/test.jpg",
                                                "public_id", "test-public-id"));

                MockMultipartFile image = new MockMultipartFile(
                                "image", "photo.jpg", "image/jpeg", "fake-image-content".getBytes());

                mockMvc.perform(multipart("/api/admin/gallery")
                                .file(image)
                                .param("caption", "Día de puertas abiertas")
                                .param("folderId", String.valueOf(createFolder()))
                                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.imageUrl").value("https://res.cloudinary.com/demo/test.jpg"))
                                .andExpect(jsonPath("$.caption").value("Día de puertas abiertas"));
        }

        @Test
        @DisplayName("An uploaded photo appears in the admin list")
        void testUpload_ThenGetAll_IncludesPhoto() throws Exception {
                when(uploader.upload(any(byte[].class), anyMap()))
                                .thenReturn(Map.of(
                                                "secure_url", "https://res.cloudinary.com/demo/list.jpg",
                                                "public_id", "list-public-id"));

                MockMultipartFile image = new MockMultipartFile(
                                "image", "photo.jpg", "image/jpeg", "fake-image-content".getBytes());

                mockMvc.perform(multipart("/api/admin/gallery")
                                .file(image)
                                .param("caption", "Taller de ciencias")
                                .param("folderId", String.valueOf(createFolder()))
                                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                                .andExpect(status().isCreated());

                mockMvc.perform(get("/api/admin/gallery")
                                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[?(@.caption == 'Taller de ciencias')]").exists());
        }

        @Test
        @DisplayName("A deleted photo no longer appears in the gallery")
        void testDelete_RemovesPhotoFromList() throws Exception {
                when(uploader.upload(any(byte[].class), anyMap()))
                                .thenReturn(Map.of(
                                                "secure_url", "https://res.cloudinary.com/demo/delete.jpg",
                                                "public_id", "delete-public-id"));

                MockMultipartFile image = new MockMultipartFile(
                                "image", "photo.jpg", "image/jpeg", "fake-image-content".getBytes());

                String createBody = mockMvc.perform(multipart("/api/admin/gallery")
                                .file(image)
                                .param("caption", "Foto a borrar")
                                .param("folderId", String.valueOf(createFolder()))
                                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                                .andExpect(status().isCreated())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                GalleryPhotoResponse created = mapper.readValue(createBody, GalleryPhotoResponse.class);

                when(uploader.destroy(anyString(), anyMap())).thenReturn(Map.of("result", "ok"));

                mockMvc.perform(delete("/api/admin/gallery/" + created.id())
                                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                                .andExpect(status().isNoContent());

                mockMvc.perform(get("/api/admin/gallery")
                                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[?(@.id == %d)]".formatted(created.id())).isEmpty());
        }

        @Test
        @DisplayName("Uploading without an image is rejected")
        void testUpload_MissingImage_ReturnsBadRequest() throws Exception {
                mockMvc.perform(multipart("/api/admin/gallery")
                                .param("caption", "Sin imagen")
                                .param("folderId", String.valueOf(createFolder()))
                                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Uploading without a folder is rejected")
        void testUpload_MissingFolderId_ReturnsBadRequest() throws Exception {
                MockMultipartFile image = new MockMultipartFile(
                                "image", "photo.jpg", "image/jpeg", "fake-image-content".getBytes());

                mockMvc.perform(multipart("/api/admin/gallery")
                                .file(image)
                                .param("caption", "Sin carpeta")
                                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("A Cloudinary failure during upload returns Bad Gateway")
        void testUpload_CloudinaryFailure_ReturnsBadGateway() throws Exception {
                when(uploader.upload(any(byte[].class), anyMap()))
                                .thenThrow(new IOException("Cloudinary no responde"));

                MockMultipartFile image = new MockMultipartFile(
                                "image", "photo.jpg", "image/jpeg", "fake-image-content".getBytes());

                mockMvc.perform(multipart("/api/admin/gallery")
                                .file(image)
                                .param("folderId", String.valueOf(createFolder()))
                                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                                .andExpect(status().isBadGateway());
        }

        @Test
        @DisplayName("Deleting a photo that does not exist returns Not Found")
        void testDelete_NonExistentPhoto_ReturnsNotFound() throws Exception {
                mockMvc.perform(delete("/api/admin/gallery/999999")
                                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("A Cloudinary failure during delete returns Bad Gateway")
        void testDelete_CloudinaryFailure_ReturnsBadGateway() throws Exception {
                when(uploader.upload(any(byte[].class), anyMap()))
                                .thenReturn(Map.of(
                                                "secure_url", "https://res.cloudinary.com/demo/delete-failure.jpg",
                                                "public_id", "delete-failure-public-id"));

                MockMultipartFile image = new MockMultipartFile(
                                "image", "photo.jpg", "image/jpeg", "fake-image-content".getBytes());

                String createBody = mockMvc.perform(multipart("/api/admin/gallery")
                                .file(image)
                                .param("caption", "Foto que falla al borrar")
                                .param("folderId", String.valueOf(createFolder()))
                                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                                .andExpect(status().isCreated())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                GalleryPhotoResponse created = mapper.readValue(createBody, GalleryPhotoResponse.class);

                when(uploader.destroy(anyString(), anyMap()))
                                .thenThrow(new IOException("Cloudinary no responde"));

                mockMvc.perform(delete("/api/admin/gallery/" + created.id())
                                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                                .andExpect(status().isBadGateway());
        }
}
