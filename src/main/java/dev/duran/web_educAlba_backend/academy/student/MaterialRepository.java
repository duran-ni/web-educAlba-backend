package dev.duran.web_educAlba_backend.academy;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    // Fichas y materiales de un alumno (US47)
    List<Material> findByStudentId(Long studentId);
}
