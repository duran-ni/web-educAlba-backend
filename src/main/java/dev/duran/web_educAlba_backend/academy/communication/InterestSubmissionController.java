package dev.duran.web_educAlba_backend.academy;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/public/interest-submissions")
public class InterestSubmissionController {

    private final InterestSubmissionService interestSubmissionService;

    public InterestSubmissionController(InterestSubmissionService interestSubmissionService) {
        this.interestSubmissionService = interestSubmissionService;
    }

    @PostMapping
    public ResponseEntity<InterestSubmissionResponse> create(@Valid @RequestBody InterestSubmissionRequest request) {
        InterestSubmissionResponse response = interestSubmissionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
