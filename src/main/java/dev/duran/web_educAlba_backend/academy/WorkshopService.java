package dev.duran.web_educAlba_backend.academy;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkshopService {

    private final WorkshopRepository workshopRepository;

    public WorkshopService(WorkshopRepository workshopRepository) {
        this.workshopRepository = workshopRepository;
    }

    @Transactional(readOnly = true)
    public List<WorkshopResponse> getAll() {
        return workshopRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkshopResponse getById(Long id) {
        Workshop workshop = findWorkshopOrThrow(id);
        return toResponse(workshop);
    }

    @Transactional
    public WorkshopResponse create(WorkshopRequest request) {
        Workshop workshop = Workshop.builder()
                .name(request.name())
                .description(request.description())
                .date(request.date())
                .recommendedAge(request.recommendedAge())
                .room(request.room())
                .active(request.active())
                .build();

        Workshop savedWorkshop = workshopRepository.save(workshop);
        return toResponse(savedWorkshop);
    }

    @Transactional
    public WorkshopResponse update(Long id, WorkshopRequest request) {
        Workshop workshop = findWorkshopOrThrow(id);

        workshop.setName(request.name());
        workshop.setDescription(request.description());
        workshop.setDate(request.date());
        workshop.setRecommendedAge(request.recommendedAge());
        workshop.setRoom(request.room());
        workshop.setActive(request.active());

        Workshop updatedWorkshop = workshopRepository.save(workshop);
        return toResponse(updatedWorkshop);
    }

    @Transactional
    public void delete(Long id) {
        Workshop workshop = findWorkshopOrThrow(id);
        workshopRepository.delete(workshop);
    }

    private Workshop findWorkshopOrThrow(Long id) {
        return workshopRepository.findById(id)
                .orElseThrow(() -> new WorkshopNotFoundException(id));
    }

    private WorkshopResponse toResponse(Workshop workshop) {
        return new WorkshopResponse(
                workshop.getId(),
                workshop.getName(),
                workshop.getDescription(),
                workshop.getDate(),
                workshop.getRecommendedAge(),
                workshop.getRoom(),
                workshop.isActive());
    }

    @Transactional(readOnly = true)
    public Optional<WorkshopResponse> getNextUpcoming() {
        return workshopRepository.findFirstByActiveTrueAndDateAfterOrderByDateAsc(LocalDate.now())
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<WorkshopResponse> getAllActive() {
        return workshopRepository.findByActiveTrue().stream()
                .sorted(Comparator.comparing(Workshop::getDate))
                .map(this::toResponse)
                .toList();
    }
}
