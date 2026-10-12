package dev.duran.web_educAlba_backend.academy.gallery;

import java.time.LocalDateTime;

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

    // URL segura (https://) que devuelve Cloudinary tras subir la imagen
    private String imageUrl;

    // Identificador interno de Cloudinary para esta imagen, necesario para
    // poder borrarla de Cloudinary cuando se elimine desde el admin
    private String publicId;

    private String caption;

    private LocalDateTime uploadedAt;
}
