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
@RequestMapping("/api/admin/agenda-events")
public class AgendaEventController {

    private final AgendaEventService agendaEventService;

    public AgendaEventController(AgendaEventService agendaEventService) {
        this.agendaEventService = agendaEventService;
    }

    @GetMapping
    public List<AgendaEventResponse> getAll() {
        return agendaEventService.getAll();
    }

    @GetMapping("/{id}")
    public AgendaEventResponse getById(@PathVariable Long id) {
        return agendaEventService.getById(id);
    }

    @PostMapping
    public ResponseEntity<AgendaEventResponse> create(@Valid @RequestBody AgendaEventRequest request) {
        AgendaEventResponse response = agendaEventService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public AgendaEventResponse update(@PathVariable Long id, @Valid @RequestBody AgendaEventRequest request) {
        return agendaEventService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        agendaEventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
