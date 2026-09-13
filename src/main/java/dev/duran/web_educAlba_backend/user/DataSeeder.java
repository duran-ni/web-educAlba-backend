package dev.duran.web_educAlba_backend.user;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import dev.duran.web_educAlba_backend.facade.encrypt.IEncryptFacade;

// Se ejecuta automaticamente al arrancar la aplicacion, antes de aceptar peticiones
@Component
public class DataSeeder implements CommandLineRunner {

    private static final String INITIAL_ADMIN_EMAIL = "admin@educalba.com";
    private static final String INITIAL_ADMIN_PASSWORD = "AdminEducAlba123";

    private final RoleService roleService;
    private final UserRepository userRepository;
    private final IEncryptFacade encryptFacade;

    public DataSeeder(RoleService roleService, UserRepository userRepository, IEncryptFacade encryptFacade) {
        this.roleService = roleService;
        this.userRepository = userRepository;
        this.encryptFacade = encryptFacade;
    }

    @Override
    public void run(String... args) {
        RoleEntity adminRole = roleService.getOrCreateRole(RoleNames.ADMIN);
        roleService.getOrCreateRole(RoleNames.FAMILY);

        seedInitialAdmin(adminRole);
    }

    private void seedInitialAdmin(RoleEntity adminRole) {
        if (userRepository.existsByEmail(INITIAL_ADMIN_EMAIL)) {
            return;
        }

        UserEntity admin = UserEntity.builder()
            .email(INITIAL_ADMIN_EMAIL)
            .password(encryptFacade.encode("bcrypt", INITIAL_ADMIN_PASSWORD))
            .roles(Set.of(adminRole))
            .build();

        userRepository.save(admin);
    }
}
