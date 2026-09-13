package dev.duran.web_educAlba_backend.academy;

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
@RequestMapping("/api/admin/workshops")
public class WorkshopController {

    private final WorkshopService workshopService;

    public WorkshopController(WorkshopService workshopService) {
        this.workshopService = workshopService;
    }

    @GetMapping
    public List<WorkshopResponse> getAll() {
        return workshopService.getAll();
    }

    @GetMapping("/{id}")
    public WorkshopResponse getById(@PathVariable Long id) {
        return workshopService.getById(id);
    }

    @PostMapping
    public ResponseEntity<WorkshopResponse> create(@Valid @RequestBody WorkshopRequest request) {
        WorkshopResponse response = workshopService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public WorkshopResponse update(@PathVariable Long id, @Valid @RequestBody WorkshopRequest request) {
        return workshopService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workshopService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
