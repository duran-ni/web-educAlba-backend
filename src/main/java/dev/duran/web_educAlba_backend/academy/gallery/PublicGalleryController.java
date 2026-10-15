package dev.duran.web_educAlba_backend.academy.gallery;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/gallery")
public class PublicGalleryController {

    private final GalleryFolderService galleryFolderService;

    public PublicGalleryController(GalleryFolderService galleryFolderService) {
        this.galleryFolderService = galleryFolderService;
    }

    @GetMapping
    public GalleryFolderPageResponse getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        return galleryFolderService.getAllWithPhotos(page, size);
    }

    @GetMapping("/{id}")
    public GalleryFolderPhotosPageResponse getOne(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        return galleryFolderService.getOneWithPhotos(id, page, size);
    }
}
