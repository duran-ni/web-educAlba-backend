package dev.duran.web_educAlba_backend.academy;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // Talleres en los que esta inscrito un alumno, con su progreso
    List<Enrollment> findByStudentId(Long studentId);
}
