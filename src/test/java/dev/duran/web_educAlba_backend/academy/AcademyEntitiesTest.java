package dev.duran.web_educAlba_backend.academy;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import dev.duran.web_educAlba_backend.user.UserEntity;

class AcademyEntitiesTest {

    @Test
    void studentStartsAsPendingWithoutUser() {
        Student student = Student.builder()
            .firstName("Lucía")
            .lastName("García")
            .educationalStage(EducationalStage.PRIMARIA)
            .build();

        assertThat(student.getStatus()).isEqualTo(StudentStatus.PENDING);
        assertThat(student.getUser()).isNull();
    }

    @Test
    void enrollmentLinksStudentAndWorkshop() {
        Student student = Student.builder().firstName("Lucía").build();
        Workshop workshop = Workshop.builder().name("Robótica").date(LocalDate.now()).build();

        Enrollment enrollment = Enrollment.builder()
            .student(student)
            .workshop(workshop)
            .enrollmentDate(LocalDate.now())
            .build();

        assertThat(enrollment.getStudent()).isEqualTo(student);
        assertThat(enrollment.getWorkshop()).isEqualTo(workshop);
    }

    @Test
    void noteCanBePinnedByAnAdminUser() {
        UserEntity admin = UserEntity.builder().email("admin@educalba.com").build();
        Student student = Student.builder().firstName("Lucía").build();

        Note note = Note.builder()
            .content("Buen trabajo esta semana")
            .date(LocalDate.now())
            .pinned(true)
            .student(student)
            .author(admin)
            .build();

        assertThat(note.isPinned()).isTrue();
        assertThat(note.getAuthor()).isEqualTo(admin);
    }
}
