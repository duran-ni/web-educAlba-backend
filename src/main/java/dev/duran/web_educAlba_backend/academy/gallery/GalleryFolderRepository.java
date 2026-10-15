package dev.duran.web_educAlba_backend.academy.gallery;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GalleryFolderRepository extends JpaRepository<GalleryFolder, Long> {

    // Carpetas en el orden en que se crearon (la general primero, luego cada taller)
    List<GalleryFolder> findAllByOrderByCreatedAtAsc();

    // Igual que el metodo anterior, pero devolviendo solo una "pagina" de
    // resultados, para la Galeria publica paginada
    Page<GalleryFolder> findAllByOrderByCreatedAtAsc(Pageable pageable);

    // La carpeta (si existe) que tiene esta foto marcada como portada,
    // necesario para poder desmarcarla si esa foto se borra
    Optional<GalleryFolder> findByCoverPhotoId(Long coverPhotoId);
}
