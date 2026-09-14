package dev.duran.web_educAlba_backend.academy;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.duran.web_educAlba_backend.user.UserEntity;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> getAll() {
        return announcementRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public AnnouncementResponse getById(Long id) {
        Announcement announcement = findAnnouncementOrThrow(id);
        return toResponse(announcement);
    }

    @Transactional
    public AnnouncementResponse create(AnnouncementRequest request, UserEntity author) {
        Announcement announcement = Announcement.builder()
            .title(request.title())
            .content(request.content())
            .date(request.date())
            .author(author)
            .build();

        Announcement savedAnnouncement = announcementRepository.save(announcement);
        return toResponse(savedAnnouncement);
    }

    @Transactional
    public AnnouncementResponse update(Long id, AnnouncementRequest request) {
        Announcement announcement = findAnnouncementOrThrow(id);

        announcement.setTitle(request.title());
        announcement.setContent(request.content());
        announcement.setDate(request.date());

        Announcement updatedAnnouncement = announcementRepository.save(announcement);
        return toResponse(updatedAnnouncement);
    }

    @Transactional
    public void delete(Long id) {
        Announcement announcement = findAnnouncementOrThrow(id);
        announcementRepository.delete(announcement);
    }

    private Announcement findAnnouncementOrThrow(Long id) {
        return announcementRepository.findById(id)
            .orElseThrow(() -> new AnnouncementNotFoundException(id));
    }

    private AnnouncementResponse toResponse(Announcement announcement) {
        UserEntity author = announcement.getAuthor();

        return new AnnouncementResponse(
            announcement.getId(),
            announcement.getTitle(),
            announcement.getContent(),
            announcement.getDate(),
            author.getId(),
            author.getEmail());
    }
}
