package dev.duran.web_educAlba_backend.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// Configuracion de CORS: permite que el frontend Vue (en otro origen/puerto)
// pueda hacer peticiones al backend, incluyendo el envio de la cookie de sesion
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Origen del frontend en desarrollo (Vite). En produccion se añadira
        // el dominio real cuando se despliegue 
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        // Imprescindible para que el navegador envie la cookie JSESSIONID
        // en peticiones cross-origin (Basic Auth con sesion por cookies)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
