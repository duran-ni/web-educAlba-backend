package dev.duran.web_educAlba_backend.user;

// Lo que se devuelve tras un registro correcto: nunca la contraseña, ni siquiera el hash
public record RegisterResponse(Long id, String email) {
}
