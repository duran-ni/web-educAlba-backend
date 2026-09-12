package dev.duran.web_educAlba_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// Beans de seguridad separados de SecurityConfiguration para evitar problemas de
// dependencias circulares con otros beans que necesitan el PasswordEncoder (ej. el registro)
@Configuration
public class BeanSecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
