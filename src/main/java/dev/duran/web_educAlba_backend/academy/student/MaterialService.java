package dev.duran.web_educAlba_backend.academy.student;

import java.io.IOException;
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

    // El SDK de Cloudinary devuelve un Map sin parametrizar (una limitacion de
    // su propia API, no nuestra). Se hace el cast explicito a Map<String, Object>
    // -sabemos por la documentacion de Cloudinary que las claves son siempre
    // texto- y se silencia el aviso del compilador justo aqui, en el unico
    // punto donde es inevitable, para que el resto del codigo ya trabaje con
    // el tipo parametrizado
    @SuppressWarnings("unchecked")
    private Map<String, Object> uploadToCloudinary(MultipartFile file) {
        try {
            // "resource_type: raw" es el que usa Cloudinary para archivos que no
            // son ni imagen ni video, como un PDF
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
        return materialRepository.findById(id)
            .orElseThrow(() -> new MaterialNotFoundException(id));
    }

    private Student findStudentOrThrow(Long studentId) {
        return studentRepository.findById(studentId)
            .orElseThrow(() -> new StudentNotFoundException(studentId));
    }

    private MaterialResponse toResponse(Material material) {
        Student student = material.getStudent();

        return new MaterialResponse(
            material.getId(),
            material.getTitle(),
            material.getDescription(),
            material.getFileUrl(),
            material.getUploadedAt(),
            student.getId(),
            student.getFirstName() + " " + student.getLastName());
    }
}
