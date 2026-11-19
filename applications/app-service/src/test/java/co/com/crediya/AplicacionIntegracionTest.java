package co.com.crediya;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

/**
 * Arranca la aplicación completa con su configuración real (solo cambian la base, por H2, y los
 * secretos obligatorios): cadena de seguridad, controladores, casos de uso, adaptadores, Flyway
 * y {@code ddl-auto: validate}. Detecta lo que los tests por capa no ven, como una propiedad
 * obligatoria sin valor, un bean faltante o una migración que no coincide con las entidades.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:crediya-auth-it;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;"
                + "DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "crediya.admin.email=admin@crediya.com",
        "crediya.admin.password=Admin12345",
        "jwt.generate-ephemeral-key=true"
})
@AutoConfigureMockMvc
class AplicacionIntegracionTest {
    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void appliesEveryMigrationAndCreatesTheInitialAdministrator() {
        List<String> migraciones = jdbcTemplate.queryForList(
                "SELECT \"version\" FROM \"flyway_schema_history\" "
                        + "WHERE \"type\" = 'SQL' AND \"success\" = TRUE ORDER BY \"installed_rank\"",
                String.class);
        String rol = jdbcTemplate.queryForObject(
                "SELECT rol FROM usuarios WHERE correo_electronico = 'admin@crediya.com'", String.class);

        assertEquals(List.of("1", "2", "3"), migraciones);
        assertEquals("ADMIN", rol);
    }

    @Test
    void adminLogsInRegistersAnAdvisorAndTheAdvisorCanOnlyRegisterClients() throws Exception {
        String admin = login("admin@crediya.com", "Admin12345");

        mockMvc.perform(post("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(usuario("asesor@crediya.com", "ASESOR", null)))
                .andExpect(status().isCreated());
        String asesor = login("asesor@crediya.com", "Clave1234");

        mockMvc.perform(post("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + asesor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(usuario("otro-admin@crediya.com", "ADMIN", null)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ROL_NO_PERMITIDO"));
        mockMvc.perform(post("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + asesor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(usuario("cliente@example.com", "CLIENTE", "1020304050")))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/v1/usuarios").param("documentos", "1020304050,999999")
                        .header("Authorization", "Bearer " + asesor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].correo_electronico").value("cliente@example.com"));
    }

    @Test
    void rejectsRequestsWithoutAValidToken() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(usuario("x@example.com", "CLIENTE", null)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.estado").value(401));
        mockMvc.perform(get("/api/v1/usuarios/documento/1020304050")
                        .header("Authorization", "Bearer no-es-un-token"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo_electronico\":\"admin@crediya.com\",\"clave\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publishesThePublicKeysAndHealthButNotTheApiDocs() throws Exception {
        String jwks = mockMvc.perform(get("/.well-known/jwks.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keys[0].kty").value("RSA"))
                .andReturn().getResponse().getContentAsString();
        assertFalse(JSON.readTree(jwks).get("keys").get(0).has("d"), "No debe publicar la clave privada");

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isNotFound());
    }

    private String login(String correo, String clave) throws Exception {
        String body = mockMvc.perform(post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo_electronico\":\"" + correo + "\",\"clave\":\"" + clave + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JSON.readTree(body).get("token").asString();
    }

    private static String usuario(String correo, String rol, String documento) {
        return """
                {
                  "nombres": "Ana",
                  "apellidos": "Pérez",
                  "correo_electronico": "%s",
                  "documento_identidad": %s,
                  "salario_base": 2500000,
                  "clave": "Clave1234",
                  "rol": "%s"
                }
                """.formatted(correo, documento == null ? "null" : "\"" + documento + "\"", rol);
    }
}
