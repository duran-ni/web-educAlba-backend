package dev.duran.web_educAlba_backend.academy.communication;

import java.time.LocalDateTime;

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
@Table(name = "interest_submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class InterestSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String studentName;
    private String courseToReinforce;
    private String phone;
    private LocalDateTime submittedAt;

    // true: la administradora ya ha revisado y gestionado esta solicitud.
    // columnDefinition fuerza un valor por defecto a nivel de base de datos,
    // necesario para que Hibernate pueda anadir esta columna NOT NULL a una
    // tabla que ya tenia filas guardadas de antes de este cambio
    @Column(columnDefinition = "boolean not null default false")
    @Builder.Default
    private boolean reviewed = false;
}
