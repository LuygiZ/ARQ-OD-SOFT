package pt.psoft.lending.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.security.interfaces.RSAPublicKey;

/**
 * Security configuration for Lending Service
 * Acts as OAuth2 Resource Server - validates JWT tokens but doesn't issue them
 */
@EnableWebSecurity
@Configuration
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    @Value("${jwt.public.key}")
    private RSAPublicKey rsaPublicKey;

    @Value("${springdoc.api-docs.path:/api-docs}")
    private String restApiDocPath;

    @Value("${springdoc.swagger-ui.path:/swagger-ui}")
    private String swaggerPath;

    private static final String ROLE_READER = "READER";
    private static final String ROLE_LIBRARIAN = "LIBRARIAN";
    private static final String ROLE_ADMIN = "ADMIN";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Disable CSRF for stateless API
        http.cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable());

        // Stateless session
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Exception handling
        http.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                .accessDeniedHandler(new BearerTokenAccessDeniedHandler()));

        // Authorization rules
        http.authorizeHttpRequests(auth -> auth
                // Swagger/OpenAPI
                .requestMatchers("/").permitAll()
                .requestMatchers(restApiDocPath + "/**").permitAll()
                .requestMatchers(swaggerPath + "/**").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()

                // Actuator health
                .requestMatchers("/actuator/**").permitAll()

                // Lending Query endpoints
                .requestMatchers(HttpMethod.GET, "/api/v1/lendings/{year}/{sequence}").hasAnyRole(ROLE_READER, ROLE_LIBRARIAN)
                .requestMatchers(HttpMethod.GET, "/api/v1/lendings/reader/**").hasAnyRole(ROLE_READER, ROLE_LIBRARIAN)
                .requestMatchers(HttpMethod.GET, "/api/v1/lendings/book/**").hasAnyRole(ROLE_READER, ROLE_LIBRARIAN)
                .requestMatchers(HttpMethod.GET, "/api/v1/lendings/overdue").hasRole(ROLE_LIBRARIAN)
                .requestMatchers(HttpMethod.GET, "/api/v1/lendings/stats/**").hasRole(ROLE_LIBRARIAN)
                .requestMatchers(HttpMethod.GET, "/api/v1/lendings/search").hasAnyRole(ROLE_READER, ROLE_LIBRARIAN)

                // Lending Command endpoints
                .requestMatchers(HttpMethod.POST, "/api/v1/lendings").hasRole(ROLE_LIBRARIAN)
                .requestMatchers(HttpMethod.POST, "/api/v1/lendings/{year}/{sequence}/return").hasRole(ROLE_READER)

                // Admin access to all
                .requestMatchers("/**").hasRole(ROLE_ADMIN)
                .anyRequest().authenticated()
        );

        // JWT authentication
        http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(rsaPublicKey).build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
