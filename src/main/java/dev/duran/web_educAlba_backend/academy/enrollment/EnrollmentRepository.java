package dev.duran.web_educAlba_backend.academy.enrollment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // Talleres en los que esta inscrito un alumno, con su progreso
    List<Enrollment> findByStudentId(Long studentId);

    // Numero de alumnos distintos con al menos una inscripcion, para el
    // indicador del Dashboard Administrador. Un alumno apuntado a varios
    // talleres cuenta una sola vez (ver DISTINCT)
    @Query("SELECT COUNT(DISTINCT e.student.id) FROM Enrollment e")
    long countDistinctStudents();
}
