package dev.duran.web_educAlba_backend.academy.teacher;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final Cloudinary cloudinary;

    public TeacherService(TeacherRepository teacherRepository, Cloudinary cloudinary) {
        this.teacherRepository = teacherRepository;
        this.cloudinary = cloudinary;
    }

    @Transactional(readOnly = true)
    public List<TeacherResponse> getAll() {
        return teacherRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TeacherResponse getById(Long id) {
        Teacher teacher = findTeacherOrThrow(id);
        return toResponse(teacher);
    }

    @Transactional
    public TeacherResponse create(TeacherRequest request) {
        Teacher teacher = Teacher.builder()
                .name(request.name())
                .specialty(request.specialty())
                .build();

        Teacher savedTeacher = teacherRepository.save(teacher);
        return toResponse(savedTeacher);
    }

    @Transactional
    public TeacherResponse update(Long id, TeacherRequest request) {
        Teacher teacher = findTeacherOrThrow(id);

        teacher.setName(request.name());
        teacher.setSpecialty(request.specialty());

        Teacher updatedTeacher = teacherRepository.save(teacher);
        return toResponse(updatedTeacher);
    }

    @Transactional
    public TeacherResponse uploadPhoto(Long id, MultipartFile image) {
        Teacher teacher = findTeacherOrThrow(id);

        // Si el profesor ya tenia una foto previa, la borramos de Cloudinary
        // antes de subir la nueva, para no dejar fotos huerfanas acumulandose
        if (teacher.getPhotoPublicId() != null) {
            deleteFromCloudinary(teacher.getPhotoPublicId());
        }

        Map<String, Object> uploadResult = uploadToCloudinary(image);

        teacher.setPhotoUrl((String) uploadResult.get("secure_url"));
        teacher.setPhotoPublicId((String) uploadResult.get("public_id"));

        Teacher updatedTeacher = teacherRepository.save(teacher);
        return toResponse(updatedTeacher);
    }

    @Transactional
    public void delete(Long id) {
        Teacher teacher = findTeacherOrThrow(id);

        // Si el profesor tenia foto subida, la borramos tambien de Cloudinary
        // para no dejar archivos huerfanos que ya no usa nadie
        if (teacher.getPhotoPublicId() != null) {
            deleteFromCloudinary(teacher.getPhotoPublicId());
        }

        teacherRepository.delete(teacher);
    }

    private Map<String, Object> uploadToCloudinary(MultipartFile image) {
        try {
            // Aqui forzamos "resource_type: image" (a diferencia de la Galeria,
            // que admite tambien videos) porque la foto de un profesor siempre
            // es una imagen
            return cloudinary.uploader().upload(image.getBytes(), ObjectUtils.asMap("resource_type", "image"));
        } catch (IOException ex) {
            throw new TeacherUploadException("subir", ex);
        }
    }

    private void deleteFromCloudinary(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        } catch (IOException ex) {
            throw new TeacherUploadException("eliminar", ex);
        }
    }

    private Teacher findTeacherOrThrow(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new TeacherNotFoundException(id));
    }

    private TeacherResponse toResponse(Teacher teacher) {
        return new TeacherResponse(
                teacher.getId(),
                teacher.getName(),
                teacher.getSpecialty(),
                teacher.getPhotoUrl());
    }
}
