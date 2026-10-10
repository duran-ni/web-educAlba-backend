package dev.duran.web_educAlba_backend.academy;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;
    private final JavaMailSender javaMailSender;
    private final String contactRecipient;
    private final String fromAddress;

    public ContactMessageService(
    ContactMessageRepository contactMessageRepository,
    JavaMailSender javaMailSender,
    @Value("${app.mail.contact-recipient}") String contactRecipient,
    @Value("${app.mail.from-address}") String fromAddress) {
    this.contactMessageRepository = contactMessageRepository;
    this.javaMailSender = javaMailSender;
    this.contactRecipient = contactRecipient;
    this.fromAddress = fromAddress;
}

    @Transactional
    public ContactMessageResponse create(ContactMessageRequest request) {
        ContactMessage contactMessage = ContactMessage.builder()
            .name(request.name())
            .email(request.email())
            .subject(request.subject())
            .message(request.message())
            .sentAt(LocalDateTime.now())
            .build();

        ContactMessage savedContactMessage = contactMessageRepository.save(contactMessage);
        sendNotificationEmail(savedContactMessage);
        return toResponse(savedContactMessage);
    }

    // Construye y envía el correo de aviso a la academia con los datos del mensaje recibido
    private void sendNotificationEmail(ContactMessage contactMessage) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(fromAddress);
        mailMessage.setTo(contactRecipient);
        mailMessage.setReplyTo(contactMessage.getEmail());
        mailMessage.setSubject("Nuevo mensaje de contacto: " + contactMessage.getSubject());
        mailMessage.setText(
            "Nombre: " + contactMessage.getName() + "\n"
                + "Email: " + contactMessage.getEmail() + "\n\n"
                + contactMessage.getMessage());

        javaMailSender.send(mailMessage);
    }

    private ContactMessageResponse toResponse(ContactMessage contactMessage) {
        return new ContactMessageResponse(
            contactMessage.getId(),
            contactMessage.getName(),
            contactMessage.getEmail(),
            contactMessage.getSubject(),
            contactMessage.getMessage(),
            contactMessage.getSentAt());
    }
}
