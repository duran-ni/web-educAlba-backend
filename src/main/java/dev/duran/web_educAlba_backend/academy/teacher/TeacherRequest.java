package dev.duran.web_educAlba_backend.academy.teacher;

import jakarta.validation.constraints.NotBlank;

// Datos de entrada para crear o actualizar un profesor
public record TeacherRequest(

    @NotBlank
    String name,

    @NotBlank
    String specialty
) {
}
