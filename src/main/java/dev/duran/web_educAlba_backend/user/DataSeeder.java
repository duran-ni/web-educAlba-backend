package dev.duran.web_educAlba_backend.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Se ejecuta automaticamente al arrancar la aplicacion, antes de aceptar peticiones
@Component
public class DataSeeder implements CommandLineRunner {

    private final RoleService roleService;

    public DataSeeder(RoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    public void run(String... args) {
        roleService.getOrCreateRole(RoleNames.ADMIN);
        roleService.getOrCreateRole(RoleNames.FAMILY);
    }
}
