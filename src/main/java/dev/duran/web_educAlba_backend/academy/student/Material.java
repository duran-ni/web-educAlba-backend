package dev.duran.web_educAlba_backend.academy.student;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "materials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    // URL segura (https://) que devuelve Cloudinary tras subir el PDF
    private String fileUrl;

    // Identificador interno de Cloudinary para este PDF, necesario para
    // poder borrarlo de Cloudinary cuando se elimine desde el admin
    private String publicId;

    // Tamaño del archivo en bytes, tal como lo devuelve Cloudinary en la
    // subida ("bytes"); se usa para mostrarlo formateado (ej. "240 KB") en
    // el listado de materiales del alumno/familia
    private Long fileSizeBytes;

    private LocalDateTime uploadedAt;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
}
