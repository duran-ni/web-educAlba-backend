package dev.duran.web_educAlba_backend.academy;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.duran.web_educAlba_backend.user.SecurityUser;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping
    public List<AnnouncementResponse> getAll() {
        return announcementService.getAll();
    }

    @GetMapping("/{id}")
    public AnnouncementResponse getById(@PathVariable Long id) {
        return announcementService.getById(id);
    }

    @PostMapping
    public ResponseEntity<AnnouncementResponse> create(@Valid @RequestBody AnnouncementRequest request,
            @AuthenticationPrincipal SecurityUser securityUser) {
        AnnouncementResponse response = announcementService.create(request, securityUser.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public AnnouncementResponse update(@PathVariable Long id, @Valid @RequestBody AnnouncementRequest request) {
        return announcementService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
