package dev.duran.web_educAlba_backend.academy.teacher;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;

    public TeacherService(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
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
    public void delete(Long id) {
        Teacher teacher = findTeacherOrThrow(id);
        teacherRepository.delete(teacher);
    }

    private Teacher findTeacherOrThrow(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new TeacherNotFoundException(id));
    }

    private TeacherResponse toResponse(Teacher teacher) {
        return new TeacherResponse(
                teacher.getId(),
                teacher.getName(),
                teacher.getSpecialty());
    }
}
