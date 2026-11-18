package co.com.crediya.api.autenticacion;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.com.crediya.api.ManejadorErroresApi;
import co.com.crediya.model.autenticacion.TokenAcceso;
import co.com.crediya.model.autenticacion.exception.CredencialesInvalidasException;
import co.com.crediya.usecase.usuario.IniciarSesionUseCase;
import co.com.crediya.model.usuario.exception.UsuarioInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class InicioSesionControllerTest {
    private static final String BODY = """
            {"correo_electronico": "ana@example.com", "clave": "Clave1234"}
            """;

    private IniciarSesionUseCase iniciarSesionUseCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        iniciarSesionUseCase = mock(IniciarSesionUseCase.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new InicioSesionController(iniciarSesionUseCase))
                .setControllerAdvice(new ManejadorErroresApi())
                .build();
    }

    @Test
    void returnsBearerTokenOnSuccessfulLogin() throws Exception {
        when(iniciarSesionUseCase.execute("ana@example.com", "Clave1234"))
                .thenReturn(new TokenAcceso("jwt-token", 3600));

        mockMvc.perform(post("/api/v1/login").contentType("application/json").content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.expira_en_segundos").value(3600));
    }

    @Test
    void returnsUnauthorizedForInvalidCredentials() throws Exception {
        when(iniciarSesionUseCase.execute("ana@example.com", "Clave1234"))
                .thenThrow(new CredencialesInvalidasException());

        mockMvc.perform(post("/api/v1/login").contentType("application/json").content(BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("CREDENCIALES_INVALIDAS"));
    }

    @Test
    void returnsBadRequestForIncompleteData() throws Exception {
        when(iniciarSesionUseCase.execute("ana@example.com", "Clave1234"))
                .thenThrow(new UsuarioInvalidoException("El correo y la clave son obligatorios."));

        mockMvc.perform(post("/api/v1/login").contentType("application/json").content(BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
    }

    @Test
    void returnsBadRequestWhenBodyIsNull() throws Exception {
        mockMvc.perform(post("/api/v1/login").contentType("application/json").content("null"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));
    }
}
