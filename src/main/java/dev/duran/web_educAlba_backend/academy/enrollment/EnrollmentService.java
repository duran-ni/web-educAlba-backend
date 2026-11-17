package dev.duran.web_educAlba_backend.academy.enrollment;

import dev.duran.web_educAlba_backend.academy.dashboard.DashboardEventsService;

import dev.duran.web_educAlba_backend.academy.workshop.Workshop;
import dev.duran.web_educAlba_backend.academy.workshop.WorkshopNotFoundException;
import dev.duran.web_educAlba_backend.academy.workshop.WorkshopRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import dev.duran.web_educAlba_backend.academy.student.Student;
import dev.duran.web_educAlba_backend.academy.student.StudentNotFoundException;
import dev.duran.web_educAlba_backend.academy.student.StudentRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final WorkshopRepository workshopRepository;
    private final DashboardEventsService dashboardEventsService;
    private final JavaMailSender javaMailSender;
    private final String contactRecipient;
    private final String fromAddress;

    public EnrollmentService(EnrollmentRepository enrollmentRepository, StudentRepository studentRepository,
            WorkshopRepository workshopRepository, DashboardEventsService dashboardEventsService,
            JavaMailSender javaMailSender,
            @Value("${app.mail.contact-recipient}") String contactRecipient,
            @Value("${app.mail.from-address}") String fromAddress) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.workshopRepository = workshopRepository;
        this.dashboardEventsService = dashboardEventsService;
        this.javaMailSender = javaMailSender;
        this.contactRecipient = contactRecipient;
        this.fromAddress = fromAddress;
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getAll() {
        return enrollmentRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public EnrollmentResponse getById(Long id) {
        Enrollment enrollment = findEnrollmentOrThrow(id);
        return toResponse(enrollment);
    }

    @Transactional
    public EnrollmentResponse create(EnrollmentRequest request) {
        Student student = findStudentOrThrow(request.studentId());
        Workshop workshop = findWorkshopOrThrow(request.workshopId());

        Enrollment enrollment = Enrollment.builder()
            .student(student)
            .workshop(workshop)
            .enrollmentDate(request.enrollmentDate())
            .progress(request.progress())
            .build();

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        dashboardEventsService.notifyChange();
        return toResponse(savedEnrollment);
    }

    @Transactional
    public EnrollmentResponse update(Long id, EnrollmentRequest request) {
        Enrollment enrollment = findEnrollmentOrThrow(id);
        Student student = findStudentOrThrow(request.studentId());
        Workshop workshop = findWorkshopOrThrow(request.workshopId());

        enrollment.setStudent(student);
        enrollment.setWorkshop(workshop);
        enrollment.setEnrollmentDate(request.enrollmentDate());
        enrollment.setProgress(request.progress());

        Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);
        return toResponse(updatedEnrollment);
    }

    @Transactional
    public void delete(Long id) {
        Enrollment enrollment = findEnrollmentOrThrow(id);
        enrollmentRepository.delete(enrollment);
        dashboardEventsService.notifyChange();
    }

    @Transactional
    public List<EnrollmentResponse> createPublic(PublicEnrollmentRequest request) {
        Student student = Student.builder()
            .firstName(request.studentName())
            .lastName("")
            .age(request.age())
            .phone(request.phone())
            .build();

        Student savedStudent = studentRepository.save(student);

        List<EnrollmentResponse> responses = request.workshopIds().stream()
            .map(workshopId -> {
                Workshop workshop = findWorkshopOrThrow(workshopId);

                Enrollment enrollment = Enrollment.builder()
                    .student(savedStudent)
                    .workshop(workshop)
                    .enrollmentDate(LocalDate.now())
                    .build();

                return toResponse(enrollmentRepository.save(enrollment));
            })
            .toList();

        dashboardEventsService.notifyChange();
        sendNotificationEmail(savedStudent, responses);
        return responses;
    }

    // Construye y envia a la academia el correo de aviso de una nueva
    // inscripcion recibida desde el formulario publico "Apuntate ya", con
    // los datos de contacto del alumno y los talleres elegidos
    private void sendNotificationEmail(Student student, List<EnrollmentResponse> responses) {
        String workshopNames = responses.stream()
            .map(EnrollmentResponse::workshopName)
            .collect(Collectors.joining(", "));

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(fromAddress);
        mailMessage.setTo(contactRecipient);
        mailMessage.setSubject("Nueva inscripcion en talleres: " + student.getFirstName());
        mailMessage.setText(
            "Nombre del alumno/a: " + student.getFirstName() + "\n"
                + "Edad: " + student.getAge() + "\n"
                + "Telefono de contacto: " + student.getPhone() + "\n\n"
                + "Talleres: " + workshopNames);

        javaMailSender.send(mailMessage);
    }

    private Enrollment findEnrollmentOrThrow(Long id) {
        return enrollmentRepository.findById(id)
            .orElseThrow(() -> new EnrollmentNotFoundException(id));
    }

    private Student findStudentOrThrow(Long studentId) {
        return studentRepository.findById(studentId)
            .orElseThrow(() -> new StudentNotFoundException(studentId));
    }

    private Workshop findWorkshopOrThrow(Long workshopId) {
        return workshopRepository.findById(workshopId)
            .orElseThrow(() -> new WorkshopNotFoundException(workshopId));
    }

    private EnrollmentResponse toResponse(Enrollment enrollment) {
        Student student = enrollment.getStudent();
        Workshop workshop = enrollment.getWorkshop();

        return new EnrollmentResponse(
            enrollment.getId(),
            student.getId(),
            student.getFirstName() + " " + student.getLastName(),
            workshop.getId(),
            workshop.getName(),
            enrollment.getEnrollmentDate(),
            enrollment.getProgress());
    }
}
