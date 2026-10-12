package dev.duran.web_educAlba_backend.academy.gallery;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/gallery")
public class GalleryPhotoController {

    private final GalleryPhotoService galleryPhotoService;

    public GalleryPhotoController(GalleryPhotoService galleryPhotoService) {
        this.galleryPhotoService = galleryPhotoService;
    }

    @GetMapping
    public List<GalleryPhotoResponse> getAll() {
        return galleryPhotoService.getAll();
    }

    @PostMapping
    public ResponseEntity<GalleryPhotoResponse> upload(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "caption", required = false) String caption) {
        GalleryPhotoResponse response = galleryPhotoService.upload(image, caption);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        galleryPhotoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
