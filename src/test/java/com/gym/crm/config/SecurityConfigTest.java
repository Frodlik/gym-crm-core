package com.gym.crm.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {
    @Test
    void testCorsConfigurationSource_whenAllowedOrigin_shouldReturnConfiguration() {
        List<String> allowedOrigins = List.of("https://example.com", "http://localhost:3000");
        CorsConfigurationSource corsSource = createCorsConfigurationSource(allowedOrigins);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/gym-crm-core/api/v1/trainees/register");

        CorsConfiguration config = corsSource.getCorsConfiguration(request);

        assertThat(config).isNotNull();
        assertThat(config.getAllowedOrigins()).containsExactlyElementsOf(allowedOrigins);
        assertThat(config.getAllowedMethods()).containsExactly("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
        assertThat(config.getAllowedHeaders()).containsExactly("*");
        assertThat(config.getAllowCredentials()).isTrue();
        assertThat(config.getMaxAge()).isEqualTo(3600L);
    }

    @Test
    void testCorsConfigurationSource_whenWildcardOrigin_shouldAllowAll() {
        List<String> allowedOrigins = List.of("*");
        CorsConfigurationSource corsSource = createCorsConfigurationSource(allowedOrigins);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/gym-crm-core/api/v1/auth/login");

        CorsConfiguration config = corsSource.getCorsConfiguration(request);

        assertThat(config).isNotNull();
        assertThat(config.getAllowedOrigins()).containsExactly("*");
        assertThat(config.getAllowCredentials()).isTrue();
    }

    private CorsConfigurationSource createCorsConfigurationSource(List<String> allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
