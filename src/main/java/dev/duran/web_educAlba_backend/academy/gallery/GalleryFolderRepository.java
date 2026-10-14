package dev.duran.web_educAlba_backend.academy.gallery;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GalleryFolderRepository extends JpaRepository<GalleryFolder, Long> {

    // Carpetas en el orden en que se crearon (la general primero, luego cada taller)
    List<GalleryFolder> findAllByOrderByCreatedAtAsc();
}
