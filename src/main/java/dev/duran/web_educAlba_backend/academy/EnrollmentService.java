package dev.duran.web_educAlba_backend.academy;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final WorkshopRepository workshopRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository, StudentRepository studentRepository,
            WorkshopRepository workshopRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.workshopRepository = workshopRepository;
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getAll() {
        return enrollmentRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public EnrollmentResponse getById(Long id) {
        Enrollment enrollment = findEnrollmentOrThrow(id);
        return toResponse(enrollment);
    }

    @Transactional
    public EnrollmentResponse create(EnrollmentRequest request) {
        Student student = findStudentOrThrow(request.studentId());
        Workshop workshop = findWorkshopOrThrow(request.workshopId());

        Enrollment enrollment = Enrollment.builder()
            .student(student)
            .workshop(workshop)
            .enrollmentDate(request.enrollmentDate())
            .progress(request.progress())
            .build();

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        return toResponse(savedEnrollment);
    }

    @Transactional
    public EnrollmentResponse update(Long id, EnrollmentRequest request) {
        Enrollment enrollment = findEnrollmentOrThrow(id);
        Student student = findStudentOrThrow(request.studentId());
        Workshop workshop = findWorkshopOrThrow(request.workshopId());

        enrollment.setStudent(student);
        enrollment.setWorkshop(workshop);
        enrollment.setEnrollmentDate(request.enrollmentDate());
        enrollment.setProgress(request.progress());

        Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);
        return toResponse(updatedEnrollment);
    }

    @Transactional
    public void delete(Long id) {
        Enrollment enrollment = findEnrollmentOrThrow(id);
        enrollmentRepository.delete(enrollment);
    }

    private Enrollment findEnrollmentOrThrow(Long id) {
        return enrollmentRepository.findById(id)
            .orElseThrow(() -> new EnrollmentNotFoundException(id));
    }

    private Student findStudentOrThrow(Long studentId) {
        return studentRepository.findById(studentId)
            .orElseThrow(() -> new StudentNotFoundException(studentId));
    }

    private Workshop findWorkshopOrThrow(Long workshopId) {
        return workshopRepository.findById(workshopId)
            .orElseThrow(() -> new WorkshopNotFoundException(workshopId));
    }

    private EnrollmentResponse toResponse(Enrollment enrollment) {
        Student student = enrollment.getStudent();
        Workshop workshop = enrollment.getWorkshop();

        return new EnrollmentResponse(
            enrollment.getId(),
            student.getId(),
            student.getFirstName() + " " + student.getLastName(),
            workshop.getId(),
            workshop.getName(),
            enrollment.getEnrollmentDate(),
            enrollment.getProgress());
    }
}
