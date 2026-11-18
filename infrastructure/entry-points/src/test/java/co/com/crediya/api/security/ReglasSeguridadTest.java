package co.com.crediya.api.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.com.crediya.api.autenticacion.InicioSesionController;
import co.com.crediya.api.autenticacion.ClavesPublicasController;
import co.com.crediya.api.usuario.RegistroUsuarioController;
import co.com.crediya.api.usuario.ConsultaUsuarioController;
import co.com.crediya.model.autenticacion.TokenAcceso;
import co.com.crediya.model.autenticacion.UsuarioAutenticado;
import co.com.crediya.model.autenticacion.exception.TokenInvalidoException;
import co.com.crediya.model.autenticacion.gateways.VerificadorTokens;
import co.com.crediya.model.usuario.Rol;
import co.com.crediya.model.usuario.Usuario;
import co.com.crediya.usecase.autenticacion.ObtenerClavesPublicasUseCase;
import co.com.crediya.usecase.usuario.ConsultarUsuarioPorDocumentoUseCase;
import co.com.crediya.usecase.usuario.ConsultarUsuariosPorDocumentosUseCase;
import co.com.crediya.usecase.usuario.IniciarSesionUseCase;
import co.com.crediya.usecase.usuario.RegistrarUsuarioUseCase;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Reglas de acceso por ruta y rol con la cadena de seguridad real ({@link SecurityConfig} y
 * {@link FiltroAutenticacionJwt}). El verificador de tokens se simula: cada token de prueba
 * es el nombre del rol.
 */
@WebMvcTest(controllers = {
        InicioSesionController.class, ClavesPublicasController.class, RegistroUsuarioController.class, ConsultaUsuarioController.class})
@Import(SecurityConfig.class)
class ReglasSeguridadTest {
    private static final String USER_BODY = """
            {"nombres":"Ana","apellidos":"Pérez","correo_electronico":"ana@example.com",
             "salario_base":1000,"clave":"Clave1234","documento_identidad":"1020304050"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VerificadorTokens verificadorTokens;
    @MockitoBean
    private IniciarSesionUseCase iniciarSesionUseCase;
    @MockitoBean
    private ObtenerClavesPublicasUseCase obtenerClavesPublicasUseCase;
    @MockitoBean
    private RegistrarUsuarioUseCase registrarUsuarioUseCase;
    @MockitoBean
    private ConsultarUsuarioPorDocumentoUseCase consultarUsuarioPorDocumentoUseCase;
    @MockitoBean
    private ConsultarUsuariosPorDocumentosUseCase consultarUsuariosPorDocumentosUseCase;

    @BeforeEach
    void setUp() {
        for (Rol rol : Rol.values()) {
            when(verificadorTokens.verificar(rol.name())).thenReturn(
                    new UsuarioAutenticado(UUID.randomUUID(), "u@crediya.com", rol, "1020304050"));
        }
        when(verificadorTokens.verificar("invalido")).thenThrow(new TokenInvalidoException("Token inválido."));
        when(registrarUsuarioUseCase.execute(any(), any())).thenReturn(Usuario.builder().build());
        when(consultarUsuarioPorDocumentoUseCase.execute(any(), any()))
                .thenReturn(Usuario.builder().documentoIdentidad("1020304050").build());
        when(consultarUsuariosPorDocumentosUseCase.execute(any(), any())).thenReturn(List.of());
        when(iniciarSesionUseCase.execute(any(), any())).thenReturn(new TokenAcceso("t", 60));
        when(obtenerClavesPublicasUseCase.execute()).thenReturn(Map.of("keys", List.of()));
    }

    @Test
    void loginAndJwksArePublic() throws Exception {
        mockMvc.perform(post("/api/v1/login").contentType("application/json")
                        .content("{\"correo_electronico\":\"a@b.co\",\"clave\":\"x\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/.well-known/jwks.json")).andExpect(status().isOk());
    }

    @Test
    void protectedRoutesRequireAValidToken() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/documento/1020304050"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"));
        mockMvc.perform(as("invalido", get("/api/v1/usuarios/documento/1020304050")))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/usuarios").contentType("application/json").content(USER_BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void onlyStaffRegistersUsers() throws Exception {
        mockMvc.perform(as("CLIENTE", registrar()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
        mockMvc.perform(as("ASESOR", registrar())).andExpect(status().isCreated());
        mockMvc.perform(as("ADMIN", registrar())).andExpect(status().isCreated());
    }

    @Test
    void onlyStaffListsUsersByDocuments() throws Exception {
        mockMvc.perform(as("CLIENTE", get("/api/v1/usuarios").param("documentos", "1020304050")))
                .andExpect(status().isForbidden());
        mockMvc.perform(as("ASESOR", get("/api/v1/usuarios").param("documentos", "1020304050")))
                .andExpect(status().isOk());
    }

    @Test
    void anyAuthenticatedUserReachesTheLookupByDocument() throws Exception {
        for (Rol rol : Rol.values()) {
            mockMvc.perform(as(rol.name(), get("/api/v1/usuarios/documento/1020304050")))
                    .andExpect(status().isOk());
        }
    }

    private static MockHttpServletRequestBuilder registrar() {
        return post("/api/v1/usuarios").contentType("application/json").content(USER_BODY);
    }

    private static MockHttpServletRequestBuilder as(String token, MockHttpServletRequestBuilder request) {
        return request.header("Authorization", "Bearer " + token);
    }
}
