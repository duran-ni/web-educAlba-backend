package dev.duran.web_educAlba_backend.academy.gallery;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GalleryFolderRepository extends JpaRepository<GalleryFolder, Long> {

    // Carpetas en el orden en que se crearon (la general primero, luego cada taller)
    List<GalleryFolder> findAllByOrderByCreatedAtAsc();

    // La carpeta (si existe) que tiene esta foto marcada como portada,
    // necesario para poder desmarcarla si esa foto se borra
    Optional<GalleryFolder> findByCoverPhotoId(Long coverPhotoId);
}
