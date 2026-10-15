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

        private GalleryPhoto createPhoto(GalleryFolder folder, String caption) {
                return galleryPhotoRepository.save(GalleryPhoto.builder()
                                .imageUrl("https://res.cloudinary.com/demo/" + caption.hashCode() + ".jpg")
                                .publicId("public-id-" + caption.hashCode())
                                .caption(caption)
                                .uploadedAt(LocalDateTime.now())
                                .folder(folder)
                                .build());
        }

        @Test
        @DisplayName("A visitor can list all photos without authentication")
        void testGetAll_ReturnsOk() throws Exception {
                long existingFolders = galleryFolderRepository.count();
                createPhoto(createFolder(), "Excursión al museo");

                mockMvc.perform(get("/api/public/gallery?size=" + (existingFolders + 1)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.folders").isArray())
                                .andExpect(jsonPath("$.folders..photos[?(@.caption == 'Excursión al museo')]")
                                                .exists());
        }

        @Test
        @DisplayName("The public response never exposes the Cloudinary public id")
        void testGetAll_DoesNotExposePublicId() throws Exception {
                long existingFolders = galleryFolderRepository.count();
                createPhoto(createFolder(), "Foto de control");

                mockMvc.perform(get("/api/public/gallery?size=" + (existingFolders + 1)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.folders..photos[?(@.caption == 'Foto de control')].publicId")
                                                .doesNotExist());
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

        @Test
        @DisplayName("A visitor can fetch a single folder by its id")
        void testGetOne_ReturnsOk() throws Exception {
                GalleryFolder folder = createFolder();
                createPhoto(folder, "Clase de robótica");

                mockMvc.perform(get("/api/public/gallery/" + folder.getId()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(folder.getId()))
                                .andExpect(jsonPath("$.photos[?(@.caption == 'Clase de robótica')]").exists());
        }

        @Test
        @DisplayName("Fetching a folder that does not exist returns 404")
        void testGetOne_ReturnsNotFound_WhenFolderDoesNotExist() throws Exception {
                mockMvc.perform(get("/api/public/gallery/999999"))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Pagination of a folder's photos returns only the requested page size "
                        + "and reports that more photos remain")
        void testGetOne_RespectsPageSizeAndReportsHasMore() throws Exception {
                GalleryFolder folder = createFolder();
                createPhoto(folder, "Foto uno");
                createPhoto(folder, "Foto dos");
                createPhoto(folder, "Foto tres");

                mockMvc.perform(get("/api/public/gallery/" + folder.getId() + "?page=0&size=2"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.photos.length()").value(2))
                                .andExpect(jsonPath("$.hasMore").value(true));
        }

        @Test
        @DisplayName("Pagination of a folder's photos reports no more photos when everything fits in the page")
        void testGetOne_HasMoreFalse_WhenAllPhotosFitInPage() throws Exception {
                GalleryFolder folder = createFolder();
                createPhoto(folder, "Única foto");

                mockMvc.perform(get("/api/public/gallery/" + folder.getId() + "?page=0&size=10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.hasMore").value(false));
        }

        @Test
        @DisplayName("The cover photo is shown first on the first page when it was already there")
        void testGetOne_PlacesCoverPhotoFirst() throws Exception {
                GalleryFolder folder = createFolder();
                createPhoto(folder, "Foto reciente");
                GalleryPhoto cover = createPhoto(folder, "Foto de portada");

                folder.setCoverPhoto(cover);
                galleryFolderRepository.save(folder);

                mockMvc.perform(get("/api/public/gallery/" + folder.getId() + "?page=0&size=10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.photos[0].caption").value("Foto de portada"));
        }
}
