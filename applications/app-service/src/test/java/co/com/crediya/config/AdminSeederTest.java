package co.com.crediya.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.com.crediya.model.user.Rol;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.exception.UserAlreadyExistsException;
import co.com.crediya.usecase.user.RegisterUserUseCase;
import org.junit.jupiter.api.Test;

class AdminSeederTest {
    private final RegisterUserUseCase registerUserUseCase = mock(RegisterUserUseCase.class);
    private final AdminSeeder seeder =
            new AdminSeeder(registerUserUseCase, "admin@crediya.com", "Admin12345");

    @Test
    void registersAdministratorThroughTheUseCase() {
        seeder.run(null);

        verify(registerUserUseCase).execute(argThat(user -> user.getRol() == Rol.ADMIN
                && "admin@crediya.com".equals(user.getCorreoElectronico())));
    }

    @Test
    void toleratesAnAdministratorThatAlreadyExists() {
        when(registerUserUseCase.execute(any(User.class))).thenThrow(new UserAlreadyExistsException());

        assertDoesNotThrow(() -> seeder.run(null));
    }
}
