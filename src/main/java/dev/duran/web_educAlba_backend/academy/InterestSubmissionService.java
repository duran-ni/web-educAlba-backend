package dev.duran.web_educAlba_backend.academy;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InterestSubmissionService {

    private final InterestSubmissionRepository interestSubmissionRepository;

    public InterestSubmissionService(InterestSubmissionRepository interestSubmissionRepository) {
        this.interestSubmissionRepository = interestSubmissionRepository;
    }

    @Transactional
    public InterestSubmissionResponse create(InterestSubmissionRequest request) {
        InterestSubmission interestSubmission = InterestSubmission.builder()
            .studentName(request.studentName())
            .courseToReinforce(request.courseToReinforce())
            .submittedAt(LocalDateTime.now())
            .build();

        InterestSubmission savedInterestSubmission = interestSubmissionRepository.save(interestSubmission);
        return toResponse(savedInterestSubmission);
    }

    private InterestSubmissionResponse toResponse(InterestSubmission interestSubmission) {
        return new InterestSubmissionResponse(
            interestSubmission.getId(),
            interestSubmission.getStudentName(),
            interestSubmission.getCourseToReinforce(),
            interestSubmission.getSubmittedAt());
    }
}
