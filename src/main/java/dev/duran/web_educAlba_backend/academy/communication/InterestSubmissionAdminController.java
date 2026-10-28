package dev.duran.web_educAlba_backend.academy.communication;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/interest-submissions")
public class InterestSubmissionAdminController {

    private final InterestSubmissionService interestSubmissionService;

    public InterestSubmissionAdminController(InterestSubmissionService interestSubmissionService) {
        this.interestSubmissionService = interestSubmissionService;
    }

    @GetMapping
    public List<InterestSubmissionResponse> getAll() {
        return interestSubmissionService.getAll();
    }

    @PutMapping("/{id}/reviewed")
    public InterestSubmissionResponse markReviewed(@PathVariable Long id) {
        return interestSubmissionService.markReviewed(id);
    }
}
