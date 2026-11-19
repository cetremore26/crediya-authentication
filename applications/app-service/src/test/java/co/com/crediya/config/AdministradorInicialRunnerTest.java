package co.com.crediya.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.com.crediya.model.usuario.Usuario;
import co.com.crediya.usecase.usuario.RegistrarUsuarioUseCase;
import org.junit.jupiter.api.Test;

class AdministradorInicialRunnerTest {
    private final RegistrarUsuarioUseCase registrarUsuarioUseCase = mock(RegistrarUsuarioUseCase.class);
    private final AdministradorInicialRunner seeder =
            new AdministradorInicialRunner(registrarUsuarioUseCase, "admin@crediya.com", "Admin12345");

    @Test
    void registersAdministratorThroughTheUseCase() {
        when(registrarUsuarioUseCase.registrarAdministradorInicial(any(Usuario.class))).thenReturn(true);

        seeder.run(null);

        verify(registrarUsuarioUseCase).registrarAdministradorInicial(argThat(usuario ->
                "admin@crediya.com".equals(usuario.getCorreoElectronico())
                        && "Admin12345".equals(usuario.getClave())));
    }

    @Test
    void toleratesAnAdministratorThatAlreadyExists() {
        when(registrarUsuarioUseCase.registrarAdministradorInicial(any(Usuario.class))).thenReturn(false);

        assertDoesNotThrow(() -> seeder.run(null));
    }
}
