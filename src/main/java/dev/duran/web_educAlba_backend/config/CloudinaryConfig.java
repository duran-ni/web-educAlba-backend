package dev.duran.web_educAlba_backend.config;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

// Configuracion del cliente de Cloudinary: crea una unica instancia (bean)
// de Cloudinary, ya autenticada con las credenciales del application.properties,
// que se inyectara en los servicios que necesiten subir o eliminar imagenes
@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        Map config = ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", "true"
        );

        return new Cloudinary(config);
    }
}
