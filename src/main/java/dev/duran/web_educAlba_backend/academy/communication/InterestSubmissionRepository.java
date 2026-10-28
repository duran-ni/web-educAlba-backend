package dev.duran.web_educAlba_backend.academy.communication;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InterestSubmissionRepository extends JpaRepository<InterestSubmission, Long> {
    List<InterestSubmission> findAllByClosedFalseOrderBySubmittedAtDesc();

    long countByReviewedFalse();
}
