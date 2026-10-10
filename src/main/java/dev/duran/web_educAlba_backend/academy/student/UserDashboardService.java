package dev.duran.web_educAlba_backend.academy.student;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.duran.web_educAlba_backend.academy.enrollment.Enrollment;
import dev.duran.web_educAlba_backend.academy.enrollment.EnrollmentRepository;
import dev.duran.web_educAlba_backend.academy.workshop.Workshop;

// Logica del Dashboard del usuario (familia/alumno): a diferencia de
// StudentService y EnrollmentService, que gestionan alumnos e inscripciones
// de cualquiera (uso del admin), este servicio siempre resuelve los datos
// a partir del id del usuario que ha iniciado sesion, nunca de un id
// recibido en la peticion
@Service
public class UserDashboardService {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;

    public UserDashboardService(StudentRepository studentRepository, EnrollmentRepository enrollmentRepository) {
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
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
