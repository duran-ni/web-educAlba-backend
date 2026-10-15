package dev.duran.web_educAlba_backend.academy.gallery;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GalleryPhotoRepository extends JpaRepository<GalleryPhoto, Long> {

    // Listado de fotos, de la mas reciente a la mas antigua
    List<GalleryPhoto> findAllByOrderByUploadedAtDesc();

    // Fotos de una carpeta concreta, de la mas reciente a la mas antigua
    List<GalleryPhoto> findByFolderIdOrderByUploadedAtDesc(Long folderId);

    // Igual que el metodo anterior, pero devolviendo solo una "pagina" de
    // resultados, para la subpagina de carpeta paginada
    Page<GalleryPhoto> findByFolderIdOrderByUploadedAtDesc(Long folderId, Pageable pageable);

    // Numero de fotos de una carpeta, usado para mostrarlo en el listado de administracion
    long countByFolderId(Long folderId);
}
