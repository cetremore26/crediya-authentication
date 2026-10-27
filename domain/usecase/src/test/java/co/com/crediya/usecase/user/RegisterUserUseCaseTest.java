package co.com.crediya.usecase.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.com.crediya.model.auth.gateways.PasswordHasher;
import co.com.crediya.model.user.Rol;
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
    private final RegisterUserUseCase useCase = new RegisterUserUseCase(repository, new FakePasswordHasher());

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

    @Test
    void registersUserWithValidDocument() {
        User saved = useCase.execute(validUser().toBuilder().documentoIdentidad(" 1020304050 ").build());

        assertEquals("1020304050", saved.getDocumentoIdentidad());
    }

    @Test
    void rejectsInvalidDocumentFormat() {
        User user = validUser().toBuilder().documentoIdentidad("12ab").build();

        assertThrows(UserValidationException.class, () -> useCase.execute(user));
    }

    @Test
    void rejectsAlreadyRegisteredDocument() {
        repository.documentAlreadyExists = true;
        User user = validUser().toBuilder().documentoIdentidad("1020304050").build();

        assertThrows(UserAlreadyExistsException.class, () -> useCase.execute(user));
    }

    private User validUser() {
        return User.builder()
                .nombres("Ana")
                .apellidos("Pérez")
                .documentoIdentidad("1020304050")
                .correoElectronico("ana@example.com")
                .salarioBase(new BigDecimal("2500000"))
                .clave("Clave1234")
                .build();
    }

    @Test
    void hashesPasswordAndDefaultsRoleToClient() {
        User saved = useCase.execute(validUser());

        assertEquals("hash:Clave1234", saved.getClave());
        assertEquals(Rol.CLIENTE, saved.getRol());
    }

    @Test
    void keepsExplicitRoleAndDoesNotRequireDocumentForStaff() {
        User saved = useCase.execute(validUser().toBuilder()
                .rol(Rol.ASESOR)
                .documentoIdentidad(null)
                .build());

        assertEquals(Rol.ASESOR, saved.getRol());
    }

    @Test
    void requiresDocumentForClients() {
        User user = validUser().toBuilder().documentoIdentidad(null).build();

        assertThrows(UserValidationException.class, () -> useCase.execute(user));
    }

    @Test
    void rejectsMissingOrOutOfRangePassword() {
        List<User> invalidUsers = List.of(
                validUser().toBuilder().clave(null).build(),
                validUser().toBuilder().clave("corta").build(),
                validUser().toBuilder().clave("x".repeat(73)).build());

        for (User user : invalidUsers) {
            assertThrows(UserValidationException.class, () -> useCase.execute(user));
        }
    }

    private static class FakePasswordHasher implements PasswordHasher {
        @Override
        public String hash(String rawPassword) {
            return "hash:" + rawPassword;
        }

        @Override
        public boolean matches(String rawPassword, String hashedPassword) {
            return hash(rawPassword).equals(hashedPassword);
        }
    }

    private static class FakeUserRepository implements UserRepository {
        private boolean emailAlreadyExists;
        private boolean documentAlreadyExists;
        private final List<User> savedUsers = new ArrayList<>();

        @Override
        public boolean existsByCorreoElectronico(String correoElectronico) {
            return emailAlreadyExists;
        }

        @Override
        public java.util.Optional<User> findByCorreoElectronico(String correoElectronico) {
            return java.util.Optional.empty();
        }

        @Override
        public boolean existsByDocumentoIdentidad(String documentoIdentidad) {
            return documentAlreadyExists;
        }

        @Override
        public java.util.Optional<User> findByDocumentoIdentidad(String documentoIdentidad) {
            return java.util.Optional.empty();
        }

        @Override
        public User save(User user) {
            savedUsers.add(user);
            return user;
        }
    }
}
