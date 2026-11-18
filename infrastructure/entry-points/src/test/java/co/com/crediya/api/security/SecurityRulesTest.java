package co.com.crediya.api.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.com.crediya.api.auth.AuthController;
import co.com.crediya.api.auth.JwksController;
import co.com.crediya.api.user.UserController;
import co.com.crediya.api.user.UserLookupController;
import co.com.crediya.model.auth.AccessToken;
import co.com.crediya.model.auth.AuthenticatedUser;
import co.com.crediya.model.auth.exception.InvalidTokenException;
import co.com.crediya.model.auth.gateways.TokenVerifier;
import co.com.crediya.model.user.Rol;
import co.com.crediya.model.user.User;
import co.com.crediya.usecase.auth.GetPublicKeySetUseCase;
import co.com.crediya.usecase.user.FindUserByDocumentUseCase;
import co.com.crediya.usecase.user.ListUsersByDocumentsUseCase;
import co.com.crediya.usecase.user.LoginUseCase;
import co.com.crediya.usecase.user.RegisterUserUseCase;
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
 * {@link JwtAuthenticationFilter}). El verificador de tokens se simula: cada token de prueba
 * es el nombre del rol.
 */
@WebMvcTest(controllers = {
        AuthController.class, JwksController.class, UserController.class, UserLookupController.class})
@Import(SecurityConfig.class)
class SecurityRulesTest {
    private static final String USER_BODY = """
            {"nombres":"Ana","apellidos":"Pérez","correo_electronico":"ana@example.com",
             "salario_base":1000,"clave":"Clave1234","documento_identidad":"1020304050"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TokenVerifier tokenVerifier;
    @MockitoBean
    private LoginUseCase loginUseCase;
    @MockitoBean
    private GetPublicKeySetUseCase getPublicKeySetUseCase;
    @MockitoBean
    private RegisterUserUseCase registerUserUseCase;
    @MockitoBean
    private FindUserByDocumentUseCase findUserByDocumentUseCase;
    @MockitoBean
    private ListUsersByDocumentsUseCase listUsersByDocumentsUseCase;

    @BeforeEach
    void setUp() {
        for (Rol rol : Rol.values()) {
            when(tokenVerifier.verify(rol.name())).thenReturn(
                    new AuthenticatedUser(UUID.randomUUID(), "u@crediya.com", rol, "1020304050"));
        }
        when(tokenVerifier.verify("invalido")).thenThrow(new InvalidTokenException("Token inválido."));
        when(registerUserUseCase.execute(any(), any())).thenReturn(User.builder().build());
        when(findUserByDocumentUseCase.execute(any(), any()))
                .thenReturn(User.builder().documentoIdentidad("1020304050").build());
        when(listUsersByDocumentsUseCase.execute(any(), any())).thenReturn(List.of());
        when(loginUseCase.execute(any(), any())).thenReturn(new AccessToken("t", 60));
        when(getPublicKeySetUseCase.execute()).thenReturn(Map.of("keys", List.of()));
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
        mockMvc.perform(as("CLIENTE", registerUser()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
        mockMvc.perform(as("ASESOR", registerUser())).andExpect(status().isCreated());
        mockMvc.perform(as("ADMIN", registerUser())).andExpect(status().isCreated());
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

    private static MockHttpServletRequestBuilder registerUser() {
        return post("/api/v1/usuarios").contentType("application/json").content(USER_BODY);
    }

    private static MockHttpServletRequestBuilder as(String token, MockHttpServletRequestBuilder request) {
        return request.header("Authorization", "Bearer " + token);
    }
}
