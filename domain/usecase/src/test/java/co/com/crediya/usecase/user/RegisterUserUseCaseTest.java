package co.com.crediya.usecase.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.com.crediya.model.user.User;
import co.com.crediya.model.user.exception.UserAlreadyExistsException;
import co.com.crediya.model.user.gateways.UserRepository;
import co.com.crediya.usecase.user.exception.UserValidationException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class RegisterUserUseCaseTest {
    private final FakeUserRepository repository = new FakeUserRepository();
    private final RegisterUserUseCase useCase = new RegisterUserUseCase(repository);

    @Test
    void registersUserAndTrimsEmail() {
        User user = validUser().toBuilder()
                .correoElectronico("  solicitante@example.com  ")
                .build();

        User saved = useCase.execute(user);

        assertEquals("solicitante@example.com", saved.getCorreoElectronico());
        assertEquals(saved, repository.savedUsers.get(0));
    }

    @Test
    void rejectsNullUser() {
        assertThrows(UserValidationException.class, () -> useCase.execute(null));
    }

    @Test
    void rejectsMissingRequiredFields() {
        List<User> invalidUsers = List.of(
                validUser().toBuilder().nombres(" ").build(),
                validUser().toBuilder().apellidos(null).build(),
                validUser().toBuilder().correoElectronico("").build(),
                validUser().toBuilder().salarioBase(null).build());

        for (User user : invalidUsers) {
            assertThrows(UserValidationException.class, () -> useCase.execute(user));
        }
        assertEquals(0, repository.savedUsers.size());
    }

    @Test
    void rejectsInvalidEmail() {
        User user = validUser().toBuilder()
                .correoElectronico("correo-invalido")
                .build();

        assertThrows(UserValidationException.class, () -> useCase.execute(user));
        assertEquals(0, repository.savedUsers.size());
    }

    @Test
    void rejectsSalaryOutsideAllowedRange() {
        List<User> invalidUsers = List.of(
                validUser().toBuilder().salarioBase(new BigDecimal("-0.01")).build(),
                validUser().toBuilder().salarioBase(new BigDecimal("15000000.01")).build());

        for (User user : invalidUsers) {
            assertThrows(UserValidationException.class, () -> useCase.execute(user));
        }
        assertEquals(0, repository.savedUsers.size());
    }

    @Test
    void acceptsSalaryAtBothRangeBoundaries() {
        useCase.execute(validUser().toBuilder().salarioBase(BigDecimal.ZERO).build());
        useCase.execute(validUser().toBuilder()
                .salarioBase(new BigDecimal("15000000"))
                .build());

        assertEquals(2, repository.savedUsers.size());
    }

    @Test
    void rejectsAlreadyRegisteredEmail() {
        repository.emailAlreadyExists = true;

        assertThrows(UserAlreadyExistsException.class, () -> useCase.execute(validUser()));
        assertEquals(0, repository.savedUsers.size());
    }

    private User validUser() {
        return User.builder()
                .nombres("Ana")
                .apellidos("Pérez")
                .correoElectronico("ana@example.com")
                .salarioBase(new BigDecimal("2500000"))
                .build();
    }

    private static class FakeUserRepository implements UserRepository {
        private boolean emailAlreadyExists;
        private final List<User> savedUsers = new ArrayList<>();

        @Override
        public boolean existsByCorreoElectronico(String correoElectronico) {
            return emailAlreadyExists;
        }

        @Override
        public User save(User user) {
            savedUsers.add(user);
            return user;
        }
    }
}
