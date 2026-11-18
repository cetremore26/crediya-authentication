package co.com.crediya.api.auth;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.com.crediya.usecase.auth.GetPublicKeySetUseCase;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class JwksControllerTest {

    @Test
    void servesThePublicKeySet() throws Exception {
        GetPublicKeySetUseCase useCase = mock(GetPublicKeySetUseCase.class);
        when(useCase.execute()).thenReturn(Map.of("keys", List.of(Map.of("kty", "RSA", "kid", "k1"))));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new JwksController(useCase)).build();

        mockMvc.perform(get("/.well-known/jwks.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keys[0].kty").value("RSA"))
                .andExpect(jsonPath("$.keys[0].kid").value("k1"));
    }
}
