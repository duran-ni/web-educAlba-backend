package dev.duran.web_educAlba_backend.academy;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getAll() {
        return studentRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public StudentResponse getById(Long id) {
        Student student = findStudentOrThrow(id);
        return toResponse(student);
    }

    @Transactional
    public StudentResponse create(StudentRequest request) {
        Student student = Student.builder()
            .firstName(request.firstName())
            .lastName(request.lastName())
            .serviceOfInterest(request.serviceOfInterest())
            .educationalStage(request.educationalStage())
            .build();

        Student savedStudent = studentRepository.save(student);
        return toResponse(savedStudent);
    }

    @Transactional
    public StudentResponse update(Long id, StudentRequest request) {
        Student student = findStudentOrThrow(id);

        student.setFirstName(request.firstName());
        student.setLastName(request.lastName());
        student.setServiceOfInterest(request.serviceOfInterest());
        student.setEducationalStage(request.educationalStage());

        Student updatedStudent = studentRepository.save(student);
        return toResponse(updatedStudent);
    }

    @Transactional
    public void delete(Long id) {
        Student student = findStudentOrThrow(id);
        studentRepository.delete(student);
    }

    private Student findStudentOrThrow(Long id) {
        return studentRepository.findById(id)
            .orElseThrow(() -> new StudentNotFoundException(id));
    }

    private StudentResponse toResponse(Student student) {
        return new StudentResponse(
            student.getId(),
            student.getFirstName(),
            student.getLastName(),
            student.getServiceOfInterest(),
            student.getEducationalStage(),
            student.getStatus());
    }
}
