package dev.duran.web_educAlba_backend.academy.gallery;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GalleryPhotoRepository extends JpaRepository<GalleryPhoto, Long> {

    // Listado de fotos, de la mas reciente a la mas antigua
    List<GalleryPhoto> findAllByOrderByUploadedAtDesc();
}
