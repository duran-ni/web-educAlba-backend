package dev.duran.web_educAlba_backend.academy;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AgendaEventRepository extends JpaRepository<AgendaEvent, Long> {

    // Agenda de un dia concreto
    List<AgendaEvent> findByDate(LocalDate date);

    // Agenda de un rango de fechas, para "esta semana" 
    List<AgendaEvent> findByDateBetween(LocalDate startDate, LocalDate endDate);
}
