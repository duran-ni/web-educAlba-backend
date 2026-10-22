package dev.duran.web_educAlba_backend.academy.student;

import java.io.IOException;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final StudentRepository studentRepository;
    private final Cloudinary cloudinary;

    public MaterialService(MaterialRepository materialRepository, StudentRepository studentRepository,
            Cloudinary cloudinary) {
        this.materialRepository = materialRepository;
        this.studentRepository = studentRepository;
        this.cloudinary = cloudinary;
    }

    @Transactional(readOnly = true)
    public List<MaterialResponse> getAll() {
        return materialRepository.findAllByOrderByUploadedAtDesc().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public MaterialResponse getById(Long id) {
        Material material = findMaterialOrThrow(id);
        return toResponse(material);
    }

    @Transactional
    public MaterialResponse upload(MultipartFile file, String title, String description, Long studentId) {
        Student student = findStudentOrThrow(studentId);
        Map<String, Object> uploadResult = uploadToCloudinary(file);

        Material material = Material.builder()
            .title(title)
            .description(description)
            .fileUrl((String) uploadResult.get("secure_url"))
            .publicId((String) uploadResult.get("public_id"))
            .uploadedAt(LocalDateTime.now())
            .student(student)
            .build();

        Material savedMaterial = materialRepository.save(material);
        return toResponse(savedMaterial);
    }

    @Transactional
    public MaterialResponse updateDetails(Long id, MaterialDetailsRequest request) {
        Material material = findMaterialOrThrow(id);
        material.setTitle(request.title());
        material.setDescription(request.description());
        Material updatedMaterial = materialRepository.save(material);
        return toResponse(updatedMaterial);
    }

    @Transactional
    public void delete(Long id) {
        Material material = findMaterialOrThrow(id);
        deleteFromCloudinary(material.getPublicId());
        materialRepository.delete(material);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> uploadToCloudinary(MultipartFile file) {
        try {
            return (Map<String, Object>) cloudinary.uploader()
                .upload(file.getBytes(), ObjectUtils.asMap("resource_type", "raw"));
        } catch (IOException ex) {
            throw new MaterialUploadException("subir", ex);
        }
    }

    private void deleteFromCloudinary(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "raw"));
        } catch (IOException ex) {
            throw new MaterialUploadException("eliminar", ex);
        }
    }

    private Material findMaterialOrThrow(Long id) {
        return materialRepository.findById(id).orElseThrow(() -> new MaterialNotFoundException(id));
    }

    private Student findStudentOrThrow(Long studentId) {
        return studentRepository.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
    }

    private MaterialResponse toResponse(Material material) {
        Student student = material.getStudent();
        return new MaterialResponse(
            material.getId(),
            material.getTitle(),
            material.getDescription(),
            material.getFileUrl(),
            buildDownloadUrl(material),
            material.getUploadedAt(),
            student.getId(),
            student.getFirstName() + " " + student.getLastName());
    }

    // Cloudinary guarda los PDF con un "public_id" aleatorio (por ejemplo
    // "x7f3a9b2c1"), sin relación con el título ni con extensión ".pdf". Esta
    // URL de descarga inserta la bandera "fl_attachment:<nombre>" justo
    // después de "/upload/" en la URL ya guardada, para que el navegador
    // descargue el archivo usando el título del material como nombre y con
    // la extensión correcta, en vez del identificador interno de Cloudinary
    private String buildDownloadUrl(Material material) {
        String fileName = slugify(material.getTitle()) + ".pdf";
        return material.getFileUrl().replace("/upload/", "/upload/fl_attachment:" + fileName + "/");
    }

    // Convierte el título en un nombre de archivo seguro para una URL:
    // separa las letras de sus tildes/diéresis (normalización NFD) y
    // descarta esas marcas, sustituye cualquier carácter que no sea letra o
    // número por un guion bajo, y limpia los guiones bajos sobrantes al
    // principio o al final
    private String slugify(String title) {
        String withoutAccents = Normalizer.normalize(title, Normalizer.Form.NFD)
            .replaceAll("[\\p{M}]", "");

        return withoutAccents
            .replaceAll("[^a-zA-Z0-9]+", "_")
            .replaceAll("^_+|_+$", "");
    }
}
