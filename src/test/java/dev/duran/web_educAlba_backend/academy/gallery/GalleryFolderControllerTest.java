package dev.duran.web_educAlba_backend.academy.gallery;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import dev.duran.web_educAlba_backend.TestcontainersConfiguration;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GalleryFolderControllerTest {

    private static final String ADMIN_EMAIL = "admin@educalba.com";
    private static final String ADMIN_PASSWORD = "AdminEducAlba123";

    @Autowired
    WebApplicationContext context;

    @Autowired
    GalleryFolderRepository galleryFolderRepository;

    @Autowired
    GalleryPhotoRepository galleryPhotoRepository;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                        .apply(SecurityMockMvcConfigurers.springSecurity())
                        .build();
    }

    @Test
    @DisplayName("A new folder can be created")
    void testCreate_ReturnsCreated() throws Exception {
        mockMvc.perform(post("/api/admin/gallery/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Taller de ciencias\"}")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Taller de ciencias"))
                .andExpect(jsonPath("$.photoCount").value(0));
    }

    @Test
    @DisplayName("A created folder appears in the admin list")
    void testCreate_ThenGetAll_IncludesFolder() throws Exception {
        mockMvc.perform(post("/api/admin/gallery/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Manos a la obra\"}")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/admin/gallery/folders")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'Manos a la obra')]").exists());
    }

    @Test
    @DisplayName("Creating a folder without a name is rejected")
    void testCreate_BlankName_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/admin/gallery/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\"}")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deleting a folder that does not exist returns Not Found")
    void testDelete_NonExistentFolder_ReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/admin/gallery/folders/999999")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("An empty folder can be deleted")
    void testDelete_EmptyFolder_ReturnsNoContent() throws Exception {
        GalleryFolder folder = galleryFolderRepository.save(GalleryFolder.builder()
                .name("Carpeta vacía")
                .createdAt(LocalDateTime.now())
                .build());

        mockMvc.perform(delete("/api/admin/gallery/folders/" + folder.getId())
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deleting a folder that still has photos returns Conflict")
    void testDelete_FolderWithPhotos_ReturnsConflict() throws Exception {
        GalleryFolder folder = galleryFolderRepository.save(GalleryFolder.builder()
                .name("Carpeta con fotos")
                .createdAt(LocalDateTime.now())
                .build());

        galleryPhotoRepository.save(GalleryPhoto.builder()
                .imageUrl("https://res.cloudinary.com/demo/keep.jpg")
                .publicId("keep-public-id")
                .uploadedAt(LocalDateTime.now())
                .folder(folder)
                .build());

        mockMvc.perform(delete("/api/admin/gallery/folders/" + folder.getId())
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isConflict());
    }
}
