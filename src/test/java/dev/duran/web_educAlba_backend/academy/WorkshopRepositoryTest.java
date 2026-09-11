package dev.duran.web_educAlba_backend.academy;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import dev.duran.web_educAlba_backend.TestcontainersConfiguration;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WorkshopRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WorkshopRepository workshopRepository;

    @Test
    void findsOnlyActiveWorkshops() {
        Workshop active = Workshop.builder().name("Robótica").date(LocalDate.now()).active(true).build();
        Workshop inactive = Workshop.builder().name("Pintura").date(LocalDate.now()).active(false).build();
        entityManager.persistAndFlush(active);
        entityManager.persistAndFlush(inactive);

        List<Workshop> result = workshopRepository.findByActiveTrue();

        assertThat(result).containsExactly(active);
    }

    @Test
    void findsNextUpcomingActiveWorkshop() {
        LocalDate today = LocalDate.now();
        Workshop past = Workshop.builder().name("Ya pasado").date(today.minusDays(5)).active(true).build();
        Workshop soon = Workshop.builder().name("El próximo").date(today.plusDays(3)).active(true).build();
        Workshop later = Workshop.builder().name("Más adelante").date(today.plusDays(10)).active(true).build();
        entityManager.persistAndFlush(past);
        entityManager.persistAndFlush(soon);
        entityManager.persistAndFlush(later);

        Optional<Workshop> result = workshopRepository.findFirstByActiveTrueAndDateAfterOrderByDateAsc(today);

        // Debe ser el mas cercano en el futuro, no cualquiera posterior a hoy
        assertThat(result).contains(soon);
    }
}
