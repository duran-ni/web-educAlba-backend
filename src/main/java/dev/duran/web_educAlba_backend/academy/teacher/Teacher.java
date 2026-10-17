package dev.duran.web_educAlba_backend.academy.teacher;

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
@Table(name = "teachers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre del profesor, tal como se muestra en su tarjeta en la página
    // pública de Quiénes Somos
    private String name;

    // Especialidad o materia que imparte el profesor
    private String specialty;

    // URL pública de la foto en Cloudinary, para mostrarla en la página
    // pública. Nulo mientras el profesor no tenga foto subida todavía
    private String photoUrl;

    // Identificador interno que usa Cloudinary para esa foto concreta,
    // necesario para poder borrarla o reemplazarla más adelante. No se
    // expone nunca al frontend (solo vive en TeacherResponse como photoUrl)
    private String photoPublicId;
}
