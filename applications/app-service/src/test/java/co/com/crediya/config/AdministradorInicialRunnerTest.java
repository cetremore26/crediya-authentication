package co.com.crediya.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.com.crediya.model.usuario.Rol;
import co.com.crediya.model.usuario.Usuario;
import co.com.crediya.model.usuario.exception.UsuarioYaRegistradoException;
import co.com.crediya.usecase.usuario.RegistrarUsuarioUseCase;
import org.junit.jupiter.api.Test;

class AdministradorInicialRunnerTest {
    private final RegistrarUsuarioUseCase registrarUsuarioUseCase = mock(RegistrarUsuarioUseCase.class);
    private final AdministradorInicialRunner seeder =
            new AdministradorInicialRunner(registrarUsuarioUseCase, "admin@crediya.com", "Admin12345");

    @Test
    void registersAdministratorThroughTheUseCase() {
        seeder.run(null);

        verify(registrarUsuarioUseCase).execute(argThat(usuario -> usuario.getRol() == Rol.ADMIN
                && "admin@crediya.com".equals(usuario.getCorreoElectronico())), eq(Rol.ADMIN));
    }

    @Test
    void toleratesAnAdministratorThatAlreadyExists() {
        when(registrarUsuarioUseCase.execute(any(Usuario.class), any(Rol.class))).thenThrow(new UsuarioYaRegistradoException());

        assertDoesNotThrow(() -> seeder.run(null));
    }
}
