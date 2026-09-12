package dev.duran.web_educAlba_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import dev.duran.web_educAlba_backend.user.RoleNames;
import jakarta.servlet.http.HttpServletResponse;

// Esquema de autenticacion Basic Auth con sesion por cookies
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Mas especifico primero: /api/auth/me exige estar autenticado,
                        // aunque caiga dentro del prefijo /api/auth/** que es publico
                        .requestMatchers("/api/auth/me").authenticated()
                        // Rutas publicas: registro/login y contenido publico de la web (Inicio,
                        // Talleres...)
                        .requestMatchers("/api/auth/**", "/api/public/**", "/error").permitAll()
                        // Panel del administrador: solo el rol ADMINISTRADOR
                        .requestMatchers("/api/admin/**").hasRole(RoleNames.ADMIN)
                        // Panel de la familia/alumno: solo el rol ALUMNO_FAMILIA
                        .requestMatchers("/api/dashboard/**").hasRole(RoleNames.FAMILY)
                        // Cualquier otra ruta no contemplada arriba: exige estar autenticado, sin rol
                        // concreto
                        .anyRequest().authenticated())
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/auth/register", "/api/auth/logout"))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .httpBasic(Customizer.withDefaults())
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, authentication) -> response
                                .setStatus(HttpServletResponse.SC_NO_CONTENT)));

        return http.build();
    }
}
