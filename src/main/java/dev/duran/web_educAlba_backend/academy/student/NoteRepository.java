package dev.duran.web_educAlba_backend.academy;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> {

    // Todas las notas de un alumno
    List<Note> findByStudentId(Long studentId);

    // Solo las notas fijadas por un profesor/admin 
    List<Note> findByStudentIdAndPinnedTrue(Long studentId);
}
