package dev.duran.web_educAlba_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import dev.duran.web_educAlba_backend.facade.decrypt.DecryptFacade;
import dev.duran.web_educAlba_backend.facade.decrypt.IDecoder;
import dev.duran.web_educAlba_backend.facade.decrypt.IDecryptFacade;
import dev.duran.web_educAlba_backend.facade.encrypt.EncryptFacade;
import dev.duran.web_educAlba_backend.facade.encrypt.IEncryptFacade;
import dev.duran.web_educAlba_backend.facade.encryptions_systems.Base64System;
import dev.duran.web_educAlba_backend.facade.encryptions_systems.BcryptSystem;

// Beans de seguridad separados de SecurityConfiguration para evitar problemas de
// dependencias circulares con otros beans que necesitan el PasswordEncoder (ej. el registro)
@Configuration
public class BeanSecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public IDecoder base64System() {
        return new Base64System();
    }

    @Bean
    public IDecryptFacade decryptFacade() {
        return new DecryptFacade(base64System());
    }

    @Bean
    public IEncryptFacade encryptFacade() {
        return new EncryptFacade(new BcryptSystem(passwordEncoder()));
    }
}
