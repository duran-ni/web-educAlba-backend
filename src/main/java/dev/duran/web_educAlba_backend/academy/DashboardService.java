package dev.duran.web_educAlba_backend.academy;

import dev.duran.web_educAlba_backend.academy.workshop.WorkshopRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final WorkshopRepository workshopRepository;
    private final EnrollmentRepository enrollmentRepository;

    public DashboardService(WorkshopRepository workshopRepository, EnrollmentRepository enrollmentRepository) {
        this.workshopRepository = workshopRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        long totalEnrolledStudents = enrollmentRepository.countDistinctStudents();
        long totalActiveWorkshops = workshopRepository.countByActiveTrue();

        return new DashboardSummaryResponse(totalEnrolledStudents, totalActiveWorkshops);
    }
}
