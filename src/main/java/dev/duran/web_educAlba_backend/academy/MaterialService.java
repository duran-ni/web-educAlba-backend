package dev.duran.web_educAlba_backend.academy;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final StudentRepository studentRepository;

    public MaterialService(MaterialRepository materialRepository, StudentRepository studentRepository) {
        this.materialRepository = materialRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public List<MaterialResponse> getAll() {
        return materialRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public MaterialResponse getById(Long id) {
        Material material = findMaterialOrThrow(id);
        return toResponse(material);
    }

    @Transactional
    public MaterialResponse create(MaterialRequest request) {
        Student student = findStudentOrThrow(request.studentId());

        Material material = Material.builder()
            .name(request.name())
            .subject(request.subject())
            .fileSize(request.fileSize())
            .filePath(request.filePath())
            .student(student)
            .build();

        Material savedMaterial = materialRepository.save(material);
        return toResponse(savedMaterial);
    }

    @Transactional
    public MaterialResponse update(Long id, MaterialRequest request) {
        Material material = findMaterialOrThrow(id);
        Student student = findStudentOrThrow(request.studentId());

        material.setName(request.name());
        material.setSubject(request.subject());
        material.setFileSize(request.fileSize());
        material.setFilePath(request.filePath());
        material.setStudent(student);

        Material updatedMaterial = materialRepository.save(material);
        return toResponse(updatedMaterial);
    }

    @Transactional
    public void delete(Long id) {
        Material material = findMaterialOrThrow(id);
        materialRepository.delete(material);
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
            material.getName(),
            material.getSubject(),
            material.getFileSize(),
            material.getFilePath(),
            student.getId(),
            student.getFirstName() + " " + student.getLastName());
    }
}
