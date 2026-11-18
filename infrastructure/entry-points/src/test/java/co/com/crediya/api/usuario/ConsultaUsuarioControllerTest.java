package co.com.crediya.api.usuario;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.com.crediya.api.ManejadorErroresApi;
import co.com.crediya.model.autenticacion.UsuarioAutenticado;
import co.com.crediya.model.autenticacion.exception.AccesoDenegadoException;
import co.com.crediya.model.usuario.Rol;
import co.com.crediya.model.usuario.Usuario;
import co.com.crediya.model.usuario.exception.UsuarioNoEncontradoException;
import co.com.crediya.usecase.usuario.ConsultarUsuarioPorDocumentoUseCase;
import co.com.crediya.usecase.usuario.ConsultarUsuariosPorDocumentosUseCase;
import co.com.crediya.model.usuario.exception.UsuarioInvalidoException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ConsultaUsuarioControllerTest {
    private static final UsuarioAutenticado ASESOR =
            new UsuarioAutenticado(UUID.randomUUID(), "asesor@crediya.com", Rol.ASESOR, null);

    private ConsultarUsuariosPorDocumentosUseCase useCase;
    private ConsultarUsuarioPorDocumentoUseCase consultarUsuarioPorDocumentoUseCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        useCase = mock(ConsultarUsuariosPorDocumentosUseCase.class);
        consultarUsuarioPorDocumentoUseCase = mock(ConsultarUsuarioPorDocumentoUseCase.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new ConsultaUsuarioController(consultarUsuarioPorDocumentoUseCase, useCase))
                .setControllerAdvice(new ManejadorErroresApi())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                ASESOR, null, List.of(new SimpleGrantedAuthority("ROLE_ASESOR"))));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsUsersForRequestedDocuments() throws Exception {
        when(useCase.execute(List.of("111111", "222222"), ASESOR)).thenReturn(List.of(Usuario.builder()
                .documentoIdentidad("111111")
                .nombres("Ana")
                .apellidos("Pérez")
                .correoElectronico("ana@example.com")
                .salarioBase(new BigDecimal("2500000"))
                .clave("hash-secreto")
                .build()));

        mockMvc.perform(get("/api/v1/usuarios").param("documentos", "111111,222222"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].documento_identidad").value("111111"))
                .andExpect(jsonPath("$[0].correo_electronico").value("ana@example.com"))
                .andExpect(jsonPath("$[0].salario_base").value(2500000))
                .andExpect(jsonPath("$[0].clave").doesNotExist());
    }

    @Test
    void returnsBadRequestWhenDocumentsAreMissing() throws Exception {
        when(useCase.execute(null, ASESOR))
                .thenThrow(new UsuarioInvalidoException("Debe indicar al menos un documento."));

        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
    }

    @Test
    void returnsUserWhenDocumentExists() throws Exception {
        when(consultarUsuarioPorDocumentoUseCase.execute("1020304050", ASESOR)).thenReturn(Usuario.builder()
                .documentoIdentidad("1020304050")
                .nombres("Ana")
                .apellidos("Pérez")
                .clave("hash-secreto")
                .build());

        mockMvc.perform(get("/api/v1/usuarios/documento/1020304050"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documento_identidad").value("1020304050"))
                .andExpect(jsonPath("$.nombres").value("Ana"))
                .andExpect(jsonPath("$.clave").doesNotExist());
    }

    @Test
    void returnsNotFoundWhenDocumentDoesNotExist() throws Exception {
        when(consultarUsuarioPorDocumentoUseCase.execute("999999", ASESOR)).thenThrow(new UsuarioNoEncontradoException());

        mockMvc.perform(get("/api/v1/usuarios/documento/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("USUARIO_NO_ENCONTRADO"));
    }

    @Test
    void returnsForbiddenWhenTheRequesterCannotSeeTheDocument() throws Exception {
        when(consultarUsuarioPorDocumentoUseCase.execute("1020304050", ASESOR))
                .thenThrow(new AccesoDenegadoException("Solo puede consultar sus propios datos."));

        mockMvc.perform(get("/api/v1/usuarios/documento/1020304050"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }
}
