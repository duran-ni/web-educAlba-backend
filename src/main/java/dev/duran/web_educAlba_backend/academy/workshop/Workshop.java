package dev.duran.web_educAlba_backend.academy.workshop;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "workshops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Workshop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private LocalDate date;
    private LocalTime time;
    private String recommendedAge;
    private String room;

    @Builder.Default
    private boolean active = true;

    // Precio del taller en euros. 0 significa que el taller es gratuito.
    // columnDefinition fuerza un valor por defecto a nivel de base de datos,
    // necesario para que Hibernate pueda anadir esta columna NOT NULL a una
    // tabla que ya tenia filas guardadas de antes de este cambio
    @Column(columnDefinition = "numeric(10,2) not null default 0")
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;
}
