package dev.duran.web_educAlba_backend.academy.student;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {

    // Util para el listado de "ultimos alumnos" del admin, filtrando por estado
    java.util.List<Student> findByStatusOrderByIdDesc(StudentStatus status);

    // Encuentra el alumno vinculado a una cuenta de usuario (familia), para
    // que esa familia pueda consultar sus propios datos en su Dashboard
    Optional<Student> findByUser_Id(Long userId);
}
