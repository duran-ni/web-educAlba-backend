package dev.duran.web_educAlba_backend.academy.student;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    // Materiales de un alumno, de los mas recientes a los mas antiguos
    List<Material> findByStudentIdOrderByUploadedAtDesc(Long studentId);

    // Listado completo para el panel de administracion, de los mas recientes
    // a los mas antiguos
    List<Material> findAllByOrderByUploadedAtDesc();
}
