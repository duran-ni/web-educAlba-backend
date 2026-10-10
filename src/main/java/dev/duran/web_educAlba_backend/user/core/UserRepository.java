package dev.duran.web_educAlba_backend.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // Necesario para que Spring Security cargue el usuario al iniciar sesion
    Optional<UserEntity> findByEmail(String email);

    // Necesario para comprobar emails duplicados en el registro 
    boolean existsByEmail(String email);
}
