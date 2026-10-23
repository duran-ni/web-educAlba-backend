package dev.duran.web_educAlba_backend.academy.student;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.duran.web_educAlba_backend.academy.enrollment.Enrollment;
import dev.duran.web_educAlba_backend.academy.enrollment.EnrollmentRepository;
import dev.duran.web_educAlba_backend.academy.workshop.Workshop;

// Lógica del Dashboard del usuario (familia/alumno): a diferencia de
// StudentService y EnrollmentService, que gestionan alumnos e inscripciones
// de cualquiera (uso del admin), este servicio siempre resuelve los datos
// a partir del id del usuario que ha iniciado sesión, nunca de un id
// recibido en la petición
@Service
public class UserDashboardService {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final MaterialService materialService;

    public UserDashboardService(StudentRepository studentRepository, EnrollmentRepository enrollmentRepository,
            MaterialService materialService) {
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.materialService = materialService;
    }

    @Transactional(readOnly = true)
    public StudentResponse getMyProfile(Long userId) {
        Student student = findStudentByUserOrThrow(userId);
        return toStudentResponse(student);
    }

    @Transactional(readOnly = true)
    public List<MyWorkshopResponse> getMyWorkshops(Long userId) {
        Student student = findStudentByUserOrThrow(userId);

        return enrollmentRepository.findByStudentId(student.getId()).stream()
            .map(this::toMyWorkshopResponse)
            .toList();
    }

    // Materiales del alumno vinculado al usuario que ha iniciado sesion.
    // Al resolver siempre el alumno a partir del userId de la sesion (nunca
    // de un studentId recibido en la peticion), un usuario familia nunca
    // puede llegar a pedir los materiales de otro alumno.
    @Transactional(readOnly = true)
    public List<MaterialResponse> getMyMaterials(Long userId) {
        Student student = findStudentByUserOrThrow(userId);
        return materialService.getByStudentId(student.getId());
    }

    private Student findStudentByUserOrThrow(Long userId) {
        return studentRepository.findByUser_Id(userId)
            .orElseThrow(() -> new IllegalStateException(
                "No se encontro un alumno vinculado al usuario " + userId));
    }

    private StudentResponse toStudentResponse(Student student) {
        return new StudentResponse(
            student.getId(),
            student.getFirstName(),
            student.getLastName(),
            student.getServiceOfInterest(),
            student.getAge(),
            student.getPhone(),
            student.getEducationalStage(),
            student.getStatus()
        );
    }

    private MyWorkshopResponse toMyWorkshopResponse(Enrollment enrollment) {
        Workshop workshop = enrollment.getWorkshop();

        return new MyWorkshopResponse(
            enrollment.getId(),
            workshop.getId(),
            workshop.getName(),
            workshop.getDate(),
            workshop.getTime(),
            enrollment.getProgress()
        );
    }
}
