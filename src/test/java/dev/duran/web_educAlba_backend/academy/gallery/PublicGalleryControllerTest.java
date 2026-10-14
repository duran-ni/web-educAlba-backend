package dev.duran.web_educAlba_backend.academy.gallery;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

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
class PublicGalleryControllerTest {

    @Autowired
    WebApplicationContext context;

    @Autowired
    GalleryPhotoRepository galleryPhotoRepository;

    @Autowired
    GalleryFolderRepository galleryFolderRepository;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    private GalleryFolder createFolder() {
        return galleryFolderRepository.save(GalleryFolder.builder()
                .name("Carpeta de prueba")
                .createdAt(LocalDateTime.now())
                .build());
    }

    @Test
    @DisplayName("A visitor can list all photos without authentication")
    void testGetAll_ReturnsOk() throws Exception {
        galleryPhotoRepository.save(GalleryPhoto.builder()
                .imageUrl("https://res.cloudinary.com/demo/sample.jpg")
                .publicId("sample-public-id")
                .caption("Excursión al museo")
                .uploadedAt(LocalDateTime.now())
                .folder(createFolder())
                .build());

        mockMvc.perform(get("/api/public/gallery"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$..photos[?(@.caption == 'Excursión al museo')]").exists());
    }

    @Test
    @DisplayName("The public response never exposes the Cloudinary public id")
    void testGetAll_DoesNotExposePublicId() throws Exception {
        galleryPhotoRepository.save(GalleryPhoto.builder()
                .imageUrl("https://res.cloudinary.com/demo/private.jpg")
                .publicId("should-not-be-exposed")
                .caption("Foto de control")
                .uploadedAt(LocalDateTime.now())
                .folder(createFolder())
                .build());

        mockMvc.perform(get("/api/public/gallery"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..photos[?(@.caption == 'Foto de control')].publicId").doesNotExist());
    }
}
