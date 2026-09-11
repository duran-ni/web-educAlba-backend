package dev.duran.web_educAlba_backend.academy;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {

    // Util para el listado de "ultimos alumnos" del admin, filtrando por estado
    java.util.List<Student> findByStatusOrderByIdDesc(StudentStatus status);
}
