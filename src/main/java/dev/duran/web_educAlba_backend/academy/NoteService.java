package dev.duran.web_educAlba_backend.academy;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.duran.web_educAlba_backend.user.UserEntity;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final StudentRepository studentRepository;

    public NoteService(NoteRepository noteRepository, StudentRepository studentRepository) {
        this.noteRepository = noteRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> getAll() {
        return noteRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public NoteResponse getById(Long id) {
        Note note = findNoteOrThrow(id);
        return toResponse(note);
    }

    @Transactional
    public NoteResponse create(NoteRequest request, UserEntity author) {
        Student student = findStudentOrThrow(request.studentId());

        Note note = Note.builder()
            .content(request.content())
            .date(request.date())
            .pinned(request.pinned())
            .student(student)
            .author(author)
            .build();

        Note savedNote = noteRepository.save(note);
        return toResponse(savedNote);
    }

    @Transactional
    public NoteResponse update(Long id, NoteRequest request) {
        Note note = findNoteOrThrow(id);
        Student student = findStudentOrThrow(request.studentId());

        note.setContent(request.content());
        note.setDate(request.date());
        note.setPinned(request.pinned());
        note.setStudent(student);

        Note updatedNote = noteRepository.save(note);
        return toResponse(updatedNote);
    }

    @Transactional
    public void delete(Long id) {
        Note note = findNoteOrThrow(id);
        noteRepository.delete(note);
    }

    private Note findNoteOrThrow(Long id) {
        return noteRepository.findById(id)
            .orElseThrow(() -> new NoteNotFoundException(id));
    }

    private Student findStudentOrThrow(Long studentId) {
        return studentRepository.findById(studentId)
            .orElseThrow(() -> new StudentNotFoundException(studentId));
    }

    private NoteResponse toResponse(Note note) {
        Student student = note.getStudent();
        UserEntity author = note.getAuthor();

        return new NoteResponse(
            note.getId(),
            note.getContent(),
            note.getDate(),
            note.isPinned(),
            student.getId(),
            student.getFirstName() + " " + student.getLastName(),
            author.getId(),
            author.getEmail());
    }
}
