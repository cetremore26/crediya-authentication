package co.com.crediya.api.user;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.com.crediya.api.ApiExceptionHandler;
import co.com.crediya.model.user.User;
import co.com.crediya.usecase.user.ListUsersByDocumentsUseCase;
import co.com.crediya.usecase.user.exception.UserValidationException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UserLookupControllerTest {
    private ListUsersByDocumentsUseCase useCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        useCase = mock(ListUsersByDocumentsUseCase.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new UserLookupController(useCase))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void returnsUsersForRequestedDocuments() throws Exception {
        when(useCase.execute(List.of("111111", "222222"))).thenReturn(List.of(User.builder()
                .documentoIdentidad("111111")
                .nombres("Ana")
                .apellidos("Pérez")
                .correoElectronico("ana@example.com")
                .salarioBase(new BigDecimal("2500000"))
                .build()));

        mockMvc.perform(get("/api/v1/usuarios").param("documentos", "111111,222222"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].documento_identidad").value("111111"))
                .andExpect(jsonPath("$[0].correo_electronico").value("ana@example.com"))
                .andExpect(jsonPath("$[0].salario_base").value(2500000));
    }

    @Test
    void returnsBadRequestWhenDocumentsAreMissing() throws Exception {
        when(useCase.execute(null))
                .thenThrow(new UserValidationException("Debe indicar al menos un documento."));

        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
    }
}
