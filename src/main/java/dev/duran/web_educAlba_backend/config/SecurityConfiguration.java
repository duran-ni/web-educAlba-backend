package dev.duran.web_educAlba_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    // Clave para firmar la cookie "remember-me" (ver application.properties)
    @Value("${app.remember-me.key}")
    private String rememberMeKey;

    // Duracion de la cookie "remember-me": 14 dias en segundos
    private static final int REMEMBER_ME_VALIDITY_SECONDS = 1209600;

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
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/auth/register", "/api/auth/logout", "/api/admin/**", "/api/public/**"))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                // Sin cabecera "WWW-Authenticate" en el 401, el navegador no abre su
                // ventana nativa de login (pensada para paginas tradicionales con
                // navegacion completa, no para una SPA). Esto no afecta a que el
                // login siga funcionando: el frontend sigue pudiendo autenticarse
                // enviando el header Authorization explicitamente (ver login() en
                // el frontend); esa cabecera solo controlaba el aviso visual del navegador
                .httpBasic(basic -> basic.authenticationEntryPoint((request, response, authException) ->
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)))
                // Emite una cookie "remember-me" adicional solo cuando el login incluye
                // el parametro "remember-me=true" (ver login() en el frontend), para
                // mantener la sesion activa incluso tras cerrar el navegador
                .rememberMe(rememberMe -> rememberMe
                        .key(rememberMeKey)
                        .tokenValiditySeconds(REMEMBER_ME_VALIDITY_SECONDS))
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, authentication) -> response
                                .setStatus(HttpServletResponse.SC_NO_CONTENT)));

        return http.build();
    }
}
