package dev.duran.web_educAlba_backend.academy;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgendaEventService {

    private final AgendaEventRepository agendaEventRepository;
    private final WorkshopRepository workshopRepository;

    public AgendaEventService(AgendaEventRepository agendaEventRepository, WorkshopRepository workshopRepository) {
        this.agendaEventRepository = agendaEventRepository;
        this.workshopRepository = workshopRepository;
    }

    @Transactional(readOnly = true)
    public List<AgendaEventResponse> getAll() {
        return agendaEventRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public AgendaEventResponse getById(Long id) {
        AgendaEvent agendaEvent = findAgendaEventOrThrow(id);
        return toResponse(agendaEvent);
    }

    @Transactional
    public AgendaEventResponse create(AgendaEventRequest request) {
        Workshop workshop = resolveWorkshop(request.workshopId());

        AgendaEvent agendaEvent = AgendaEvent.builder()
            .date(request.date())
            .time(request.time())
            .activity(request.activity())
            .room(request.room())
            .workshop(workshop)
            .build();

        AgendaEvent savedAgendaEvent = agendaEventRepository.save(agendaEvent);
        return toResponse(savedAgendaEvent);
    }

    @Transactional
    public AgendaEventResponse update(Long id, AgendaEventRequest request) {
        AgendaEvent agendaEvent = findAgendaEventOrThrow(id);
        Workshop workshop = resolveWorkshop(request.workshopId());

        agendaEvent.setDate(request.date());
        agendaEvent.setTime(request.time());
        agendaEvent.setActivity(request.activity());
        agendaEvent.setRoom(request.room());
        agendaEvent.setWorkshop(workshop);

        AgendaEvent updatedAgendaEvent = agendaEventRepository.save(agendaEvent);
        return toResponse(updatedAgendaEvent);
    }

    @Transactional
    public void delete(Long id) {
        AgendaEvent agendaEvent = findAgendaEventOrThrow(id);
        agendaEventRepository.delete(agendaEvent);
    }

    private AgendaEvent findAgendaEventOrThrow(Long id) {
        return agendaEventRepository.findById(id)
            .orElseThrow(() -> new AgendaEventNotFoundException(id));
    }

    private Workshop resolveWorkshop(Long workshopId) {
        if (workshopId == null) {
            return null;
        }

        return workshopRepository.findById(workshopId)
            .orElseThrow(() -> new WorkshopNotFoundException(workshopId));
    }

    private AgendaEventResponse toResponse(AgendaEvent agendaEvent) {
        Workshop workshop = agendaEvent.getWorkshop();

        Long workshopId = workshop != null ? workshop.getId() : null;
        String workshopName = workshop != null ? workshop.getName() : null;

        return new AgendaEventResponse(
            agendaEvent.getId(),
            agendaEvent.getDate(),
            agendaEvent.getTime(),
            agendaEvent.getActivity(),
            agendaEvent.getRoom(),
            workshopId,
            workshopName);
    }
}
