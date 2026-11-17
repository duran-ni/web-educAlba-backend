package dev.duran.web_educAlba_backend.academy.workshop;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkshopRepository extends JpaRepository<Workshop, Long> {

    // Listado publico de talleres activos y aun no pasados (su fecha es
    // hoy o posterior), para que un taller desaparezca automaticamente de
    // la web publica en cuanto pasa su fecha, sin tener que desactivarlo a mano
    List<Workshop> findByActiveTrueAndDateGreaterThanEqual(LocalDate date);

    // Proximo taller destacado en Inicio: el mas cercano en el futuro
    Optional<Workshop> findFirstByActiveTrueAndDateAfterOrderByDateAsc(LocalDate date);

    // Numero de talleres activos, para el indicador del Dashboard Administrador
    long countByActiveTrue();
}
