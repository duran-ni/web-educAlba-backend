package dev.duran.web_educAlba_backend.academy.communication;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class AnnouncementTest {

    @Test
    void testBuilder() {
        Announcement announcement = Announcement.builder().title("foo").build();

        assertEquals(Announcement.class, announcement.getClass());
    }
}
