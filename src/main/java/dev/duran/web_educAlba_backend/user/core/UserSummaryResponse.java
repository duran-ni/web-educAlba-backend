package dev.duran.web_educAlba_backend.user;

import java.util.List;

// Datos del usuario autenticado que el frontend necesita tras el login
// (para decidir a que dashboard redirigir, segun los roles)
public record UserSummaryResponse(Long id, String email, List<String> roles) {
}
