package co.com.crediya.api.security;

import co.com.crediya.api.autenticacion.ClavesPublicasController;
import co.com.crediya.model.autenticacion.gateways.VerificadorTokens;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final String ROL_ADMIN = "ADMIN";
    private static final String ROL_ASESOR = "ASESOR";
    private static final String[] RUTAS_PUBLICAS = {
            "/api/v1/login", ClavesPublicasController.RUTA_JWKS, "/actuator/health", "/actuator/health/**",
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, VerificadorTokens verificadorTokens)
            throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(RUTAS_PUBLICAS).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/usuarios")
                        .hasAnyRole(ROL_ADMIN, ROL_ASESOR)
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios")
                        .hasAnyRole(ROL_ADMIN, ROL_ASESOR)
                        .anyRequest().authenticated())
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(RespuestasErrorSeguridad.noAutenticado())
                        .accessDeniedHandler(RespuestasErrorSeguridad.accesoDenegado()))
                .addFilterBefore(
                        new FiltroAutenticacionJwt(verificadorTokens),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** Desactiva el usuario en memoria por defecto de Spring Boot: solo se autentica con JWT. */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("Autenticación solo por token.");
        };
    }
}
