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
public class PublicGalleryControllerTest {

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
        long existingFolders = galleryFolderRepository.count();
        galleryPhotoRepository.save(GalleryPhoto.builder()
                .imageUrl("https://res.cloudinary.com/demo/sample.jpg")
                .publicId("sample-public-id")
                .caption("Excursión al museo")
                .uploadedAt(LocalDateTime.now())
                .folder(createFolder())
                .build());

        mockMvc.perform(get("/api/public/gallery?size=" + (existingFolders + 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.folders").isArray())
                .andExpect(jsonPath("$.folders..photos[?(@.caption == 'Excursión al museo')]").exists());
    }

    @Test
    @DisplayName("The public response never exposes the Cloudinary public id")
    void testGetAll_DoesNotExposePublicId() throws Exception {
        long existingFolders = galleryFolderRepository.count();
        galleryPhotoRepository.save(GalleryPhoto.builder()
                .imageUrl("https://res.cloudinary.com/demo/private.jpg")
                .publicId("should-not-be-exposed")
                .caption("Foto de control")
                .uploadedAt(LocalDateTime.now())
                .folder(createFolder())
                .build());

        mockMvc.perform(get("/api/public/gallery?size=" + (existingFolders + 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.folders..photos[?(@.caption == 'Foto de control')].publicId").doesNotExist());
    }

    @Test
    @DisplayName("Pagination returns only the requested page size and reports that more folders remain")
    void testGetAll_RespectsPageSizeAndReportsHasMore() throws Exception {
        long existingFolders = galleryFolderRepository.count();
        createFolder();
        createFolder();

        long size = existingFolders + 1;

        mockMvc.perform(get("/api/public/gallery?page=0&size=" + size))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.folders.length()").value((int) size))
                .andExpect(jsonPath("$.hasMore").value(true));
    }

    @Test
    @DisplayName("Pagination reports no more folders when everything fits in the page")
    void testGetAll_HasMoreFalse_WhenAllFoldersFitInPage() throws Exception {
        createFolder();
        long totalFolders = galleryFolderRepository.count();

        mockMvc.perform(get("/api/public/gallery?page=0&size=" + totalFolders))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasMore").value(false));
    }
}
