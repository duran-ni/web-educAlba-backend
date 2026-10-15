package dev.duran.web_educAlba_backend.academy.student;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class StudentTest {

    @Test
    void testBuilder() {
        Student student = Student.builder().age(25).build();

        assertEquals(Student.class, student.getClass());
    }
}
