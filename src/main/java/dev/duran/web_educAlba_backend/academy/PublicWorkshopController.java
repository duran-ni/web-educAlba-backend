package dev.duran.web_educAlba_backend.academy;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/workshops")
public class PublicWorkshopController {

    private final WorkshopService workshopService;

    public PublicWorkshopController(WorkshopService workshopService) {
        this.workshopService = workshopService;
    }

    @GetMapping("/next")
    public ResponseEntity<WorkshopResponse> getNext() {
        return workshopService.getNextUpcoming()
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
