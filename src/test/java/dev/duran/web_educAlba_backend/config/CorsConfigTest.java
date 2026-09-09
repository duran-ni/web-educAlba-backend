package dev.duran.web_educAlba_backend.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

class CorsConfigTest {

    @Test
    void corsConfigurationAllowsCredentialsFromFrontendOrigin() {
        CorsConfigurationSource source = new CorsConfig().corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest();

        CorsConfiguration configuration = source.getCorsConfiguration(request);

        assertThat(configuration.getAllowedOrigins()).contains("http://localhost:5173");
        assertThat(configuration.getAllowCredentials()).isTrue();
    }
}
