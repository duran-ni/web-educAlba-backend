package dev.duran.web_educAlba_backend.academy.gallery;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/gallery")
public class PublicGalleryController {

    private final GalleryPhotoService galleryPhotoService;

    public PublicGalleryController(GalleryPhotoService galleryPhotoService) {
        this.galleryPhotoService = galleryPhotoService;
    }

    @GetMapping
    public List<GalleryPhotoResponse> getAll() {
        return galleryPhotoService.getAll();
    }
}
