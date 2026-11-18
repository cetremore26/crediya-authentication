package co.com.crediya.api;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.com.crediya.api.usuario.RegistroUsuarioController;
import co.com.crediya.usecase.usuario.RegistrarUsuarioUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** Los errores propios de Spring MVC conservan su estado HTTP en vez de convertirse en 500. */
class ManejadorErroresApiTest {
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new RegistroUsuarioController(mock(RegistrarUsuarioUseCase.class)))
                .setControllerAdvice(new ManejadorErroresApi())
                .build();
    }

    @Test
    void unsupportedMethodReturns405() throws Exception {
        mockMvc.perform(delete("/api/v1/usuarios"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.estado").value(405))
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_NO_SOPORTADA"));
    }

    @Test
    void unsupportedMediaTypeReturns415() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios").contentType("text/plain").content("hola"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_NO_SOPORTADA"));
    }
}
