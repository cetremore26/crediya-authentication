package co.com.crediya.api.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.com.crediya.api.ApiExceptionHandler;
import co.com.crediya.model.user.User;
import co.com.crediya.usecase.user.RegisterUserUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UserControllerTest {
    private RegisterUserUseCase registerUserUseCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        registerUserUseCase = org.mockito.Mockito.mock(RegisterUserUseCase.class);
        UserController controller = new UserController(registerUserUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void registersUserAndReturnsCreated() throws Exception {
        when(registerUserUseCase.execute(any(User.class))).thenReturn(User.builder().build());

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType("application/json")
                        .content("""
                                {
                                  "nombres": "Ana",
                                  "apellidos": "Pérez",
                                  "fecha_nacimiento": "1990-05-12",
                                  "direccion": "Calle 1",
                                  "telefono": "3001234567",
                                  "correo_electronico": "ana@example.com",
                                  "salario_base": 2500000
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").value("Usuario registrado exitosamente."));

        verify(registerUserUseCase).execute(any(User.class));
    }

    @Test
    void returnsBadRequestForMalformedRequestBody() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType("application/json")
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));
    }

    @Test
    void returnsBadRequestWhenRequestBodyIsNull() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType("application/json")
                        .content("null"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));
    }
}
