package dev.duran.web_educAlba_backend.academy.student;

import java.time.LocalDateTime;

public record MaterialResponse(
    Long id,
    String title,
    String description,
    String fileUrl,
    String downloadUrl,
    Long fileSizeBytes,
    LocalDateTime uploadedAt,
    Long studentId,
    String studentFullName
) {}
