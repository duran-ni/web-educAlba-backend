package dev.duran.web_educAlba_backend.academy.dashboard;

import dev.duran.web_educAlba_backend.academy.communication.InterestSubmissionRepository;
import dev.duran.web_educAlba_backend.academy.enrollment.EnrollmentRepository;
import dev.duran.web_educAlba_backend.academy.workshop.WorkshopRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final WorkshopRepository workshopRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final InterestSubmissionRepository interestSubmissionRepository;

    public DashboardService(
        WorkshopRepository workshopRepository,
        EnrollmentRepository enrollmentRepository,
        InterestSubmissionRepository interestSubmissionRepository) {
        this.workshopRepository = workshopRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.interestSubmissionRepository = interestSubmissionRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        long totalEnrolledStudents = enrollmentRepository.countDistinctStudents();
        long totalActiveWorkshops = workshopRepository.countByActiveTrue();
        long pendingInterestSubmissions = interestSubmissionRepository.countByReviewedFalse();

        return new DashboardSummaryResponse(totalEnrolledStudents, totalActiveWorkshops, pendingInterestSubmissions);
    }
}
