package co.com.crediya.api.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.com.crediya.api.ApiExceptionHandler;
import co.com.crediya.model.auth.AuthenticatedUser;
import co.com.crediya.model.user.Rol;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.exception.RolNoPermitidoException;
import co.com.crediya.usecase.user.RegisterUserUseCase;
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

class UserControllerTest {
    private static final String VALID_BODY = """
            {
              "nombres": "Ana",
              "apellidos": "Pérez",
              "fecha_nacimiento": "1990-05-12",
              "direccion": "Calle 1",
              "telefono": "3001234567",
              "correo_electronico": "ana@example.com",
              "salario_base": 2500000
            }
            """;

    private RegisterUserUseCase registerUserUseCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        registerUserUseCase = org.mockito.Mockito.mock(RegisterUserUseCase.class);
        UserController controller = new UserController(registerUserUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registersUserWithTheRoleOfTheRequester() throws Exception {
        authenticateAs(Rol.ASESOR);
        when(registerUserUseCase.execute(any(User.class), eq(Rol.ASESOR)))
                .thenReturn(User.builder().build());

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType("application/json")
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").value("Usuario registrado exitosamente."));

        verify(registerUserUseCase).execute(any(User.class), eq(Rol.ASESOR));
    }

    @Test
    void returnsForbiddenWhenTheRequesterCannotAssignTheRole() throws Exception {
        authenticateAs(Rol.ASESOR);
        when(registerUserUseCase.execute(any(User.class), eq(Rol.ASESOR)))
                .thenThrow(new RolNoPermitidoException(Rol.ADMIN));

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType("application/json")
                        .content(VALID_BODY.replace("\"salario_base\"", "\"rol\": \"ADMIN\", \"salario_base\"")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ROL_NO_PERMITIDO"));
    }

    @Test
    void returnsBadRequestForMalformedRequestBody() throws Exception {
        authenticateAs(Rol.ADMIN);

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType("application/json")
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));
    }

    @Test
    void returnsBadRequestWhenRequestBodyIsNull() throws Exception {
        authenticateAs(Rol.ADMIN);

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType("application/json")
                        .content("null"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));
    }

    private static void authenticateAs(Rol rol) {
        AuthenticatedUser user = new AuthenticatedUser(
                UUID.randomUUID(), rol.name().toLowerCase() + "@crediya.com", rol, null);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()))));
    }
}
