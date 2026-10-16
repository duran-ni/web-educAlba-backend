package dev.duran.web_educAlba_backend.academy.gallery;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "gallery_photos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class GalleryPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // URL segura (https://) que devuelve Cloudinary tras subir la imagen o el video
    private String imageUrl;

    // Identificador interno de Cloudinary para esta imagen o video, necesario
    // para poder borrarlo de Cloudinary cuando se elimine desde el admin
    private String publicId;

    // Indica si el archivo subido es una fotografia o un video, para que el
    // frontend sepa si debe mostrarlo con <img> o con <video>
    @Enumerated(EnumType.STRING)
    private GalleryMediaType mediaType;

    private String caption;

    private LocalDateTime uploadedAt;

    // No se marca "nullable = false" a nivel de columna: no usamos Flyway y
    // ya existen fotos sin carpeta asignada en la base de datos actual.
    // La obligatoriedad se garantiza en GalleryPhotoService, no en el esquema
    @ManyToOne
    @JoinColumn(name = "folder_id")
    private GalleryFolder folder;
}
