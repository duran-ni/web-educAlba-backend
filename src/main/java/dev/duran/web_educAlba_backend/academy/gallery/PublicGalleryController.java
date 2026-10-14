package dev.duran.web_educAlba_backend.academy.gallery;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/gallery")
public class PublicGalleryController {

    private final GalleryFolderService galleryFolderService;

    public PublicGalleryController(GalleryFolderService galleryFolderService) {
        this.galleryFolderService = galleryFolderService;
    }

    @GetMapping
    public List<GalleryFolderWithPhotosResponse> getAll() {
        return galleryFolderService.getAllWithPhotos();
    }
}
