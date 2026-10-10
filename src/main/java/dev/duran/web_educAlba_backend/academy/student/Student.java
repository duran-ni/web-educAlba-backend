package dev.duran.web_educAlba_backend.academy;

import dev.duran.web_educAlba_backend.user.UserEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String serviceOfInterest;
    private Integer age;
    private String phone;

    @Enumerated(EnumType.STRING)
    private EducationalStage educationalStage;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private StudentStatus status = StudentStatus.PENDING;

    // Nulo mientras el alumno es solo un "lead" sin cuenta de usuario propia
    @OneToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
