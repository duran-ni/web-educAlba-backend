package dev.duran.web_educAlba_backend.academy;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkshopRepository extends JpaRepository<Workshop, Long> {

    // Listado de talleres activos
    List<Workshop> findByActiveTrue();

    // Proximo taller destacado en Inicio: el mas cercano en el futuro
    Optional<Workshop> findFirstByActiveTrueAndDateAfterOrderByDateAsc(LocalDate date);
}
