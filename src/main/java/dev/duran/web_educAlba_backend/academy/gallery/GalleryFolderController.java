package dev.duran.web_educAlba_backend.academy.gallery;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/gallery/folders")
public class GalleryFolderController {

    private final GalleryFolderService galleryFolderService;

    public GalleryFolderController(GalleryFolderService galleryFolderService) {
        this.galleryFolderService = galleryFolderService;
    }

    @GetMapping
    public List<GalleryFolderResponse> getAll() {
        return galleryFolderService.getAll();
    }

    @PostMapping
    public ResponseEntity<GalleryFolderResponse> create(@Valid @RequestBody GalleryFolderRequest request) {
        GalleryFolderResponse response = galleryFolderService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/cover")
    public GalleryFolderResponse setCoverPhoto(
            @PathVariable Long id, @Valid @RequestBody GalleryFolderCoverRequest request) {
        return galleryFolderService.setCoverPhoto(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        galleryFolderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
