package dev.duran.web_educAlba_backend.academy;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/public/enrollments")
public class PublicEnrollmentController {

    private final EnrollmentService enrollmentService;

    public PublicEnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    public ResponseEntity<List<EnrollmentResponse>> create(@Valid @RequestBody PublicEnrollmentRequest request) {
        List<EnrollmentResponse> response = enrollmentService.createPublic(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
