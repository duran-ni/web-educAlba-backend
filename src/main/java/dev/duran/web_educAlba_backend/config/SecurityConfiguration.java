package dev.duran.web_educAlba_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import dev.duran.web_educAlba_backend.user.RoleNames;

// Esquema de autenticacion Basic Auth con sesion por cookies
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Rutas publicas: registro/login y contenido publico de la web (Inicio, Talleres...)
                .requestMatchers("/api/auth/**", "/api/public/**").permitAll()
                // Panel del administrador: solo el rol ADMINISTRADOR
                .requestMatchers("/api/admin/**").hasRole(RoleNames.ADMIN)
                // Panel de la familia/alumno: solo el rol ALUMNO_FAMILIA
                .requestMatchers("/api/dashboard/**").hasRole(RoleNames.FAMILY)
                // Cualquier otra ruta no contemplada arriba: exige estar autenticado, sin rol concreto
                .anyRequest().authenticated())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
