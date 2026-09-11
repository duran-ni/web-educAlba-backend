package dev.duran.web_educAlba_backend.academy;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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
class AgendaEventRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AgendaEventRepository agendaEventRepository;

    @Test
    void findsEventsOfAGivenDay() {
        LocalDate day = LocalDate.now();
        AgendaEvent todayEvent = AgendaEvent.builder().date(day).time(LocalTime.of(10, 0)).activity("Refuerzo Matemáticas").room("Sala 1").build();
        AgendaEvent otherDayEvent = AgendaEvent.builder().date(day.plusDays(1)).time(LocalTime.of(11, 0)).activity("Taller Robótica").room("Sala 2").build();
        entityManager.persistAndFlush(todayEvent);
        entityManager.persistAndFlush(otherDayEvent);

        List<AgendaEvent> result = agendaEventRepository.findByDate(day);

        assertThat(result).containsExactly(todayEvent);
    }

    @Test
    void findsEventsWithinADateRange() {
        LocalDate day = LocalDate.now();
        AgendaEvent inRange = AgendaEvent.builder().date(day.plusDays(2)).time(LocalTime.of(9, 0)).activity("Evento en rango").room("Sala 1").build();
        AgendaEvent outOfRange = AgendaEvent.builder().date(day.plusDays(10)).time(LocalTime.of(9, 0)).activity("Evento fuera de rango").room("Sala 1").build();
        entityManager.persistAndFlush(inRange);
        entityManager.persistAndFlush(outOfRange);

        List<AgendaEvent> result = agendaEventRepository.findByDateBetween(day, day.plusDays(5));

        assertThat(result).containsExactly(inRange);
    }
}
