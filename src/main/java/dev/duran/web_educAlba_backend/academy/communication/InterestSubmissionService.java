package dev.duran.web_educAlba_backend.academy.communication;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.duran.web_educAlba_backend.academy.dashboard.DashboardEventsService;

@Service
public class InterestSubmissionService {

    private final InterestSubmissionRepository interestSubmissionRepository;
    private final JavaMailSender javaMailSender;
    private final DashboardEventsService dashboardEventsService;
    private final String contactRecipient;
    private final String fromAddress;

    public InterestSubmissionService(
        InterestSubmissionRepository interestSubmissionRepository,
        JavaMailSender javaMailSender,
        DashboardEventsService dashboardEventsService,
        @Value("${app.mail.contact-recipient}") String contactRecipient,
        @Value("${app.mail.from-address}") String fromAddress) {
        this.interestSubmissionRepository = interestSubmissionRepository;
        this.javaMailSender = javaMailSender;
        this.dashboardEventsService = dashboardEventsService;
        this.contactRecipient = contactRecipient;
        this.fromAddress = fromAddress;
    }

    @Transactional
    public InterestSubmissionResponse create(InterestSubmissionRequest request) {
        InterestSubmission interestSubmission = InterestSubmission.builder()
            .studentName(request.studentName())
            .courseToReinforce(request.courseToReinforce())
            .phone(request.phone())
            .submittedAt(LocalDateTime.now())
            .build();

        InterestSubmission savedInterestSubmission = interestSubmissionRepository.save(interestSubmission);
        sendNotificationEmail(savedInterestSubmission);
        dashboardEventsService.notifyChange();
        return toResponse(savedInterestSubmission);
    }

    @Transactional(readOnly = true)
    public List<InterestSubmissionResponse> getAll() {
        return interestSubmissionRepository.findAllByOrderBySubmittedAtDesc().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public InterestSubmissionResponse markReviewed(Long id) {
        InterestSubmission interestSubmission = interestSubmissionRepository.findById(id)
            .orElseThrow(() -> new InterestSubmissionNotFoundException(id));

        interestSubmission.setReviewed(true);
        InterestSubmission savedInterestSubmission = interestSubmissionRepository.save(interestSubmission);
        dashboardEventsService.notifyChange();
        return toResponse(savedInterestSubmission);
    }

    // Construye y envía el correo de aviso a la academia con los datos de la solicitud de interés
    private void sendNotificationEmail(InterestSubmission interestSubmission) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(fromAddress);
        mailMessage.setTo(contactRecipient);
        mailMessage.setSubject("Nueva solicitud de interés: " + interestSubmission.getStudentName());
        mailMessage.setText(
            "Nombre del alumno/a: " + interestSubmission.getStudentName() + "\n"
                + "Curso a reforzar: " + interestSubmission.getCourseToReinforce() + "\n"
                + "Teléfono de contacto: " + interestSubmission.getPhone());

        javaMailSender.send(mailMessage);
    }

    private InterestSubmissionResponse toResponse(InterestSubmission interestSubmission) {
        return new InterestSubmissionResponse(
            interestSubmission.getId(),
            interestSubmission.getStudentName(),
            interestSubmission.getCourseToReinforce(),
            interestSubmission.getPhone(),
            interestSubmission.getSubmittedAt(),
            interestSubmission.isReviewed());
    }
}
