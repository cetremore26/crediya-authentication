package co.com.crediya.usecase.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.com.crediya.model.auth.AccessToken;
import co.com.crediya.model.auth.exception.InvalidCredentialsException;
import co.com.crediya.model.auth.gateways.PasswordHasher;
import co.com.crediya.model.auth.gateways.TokenProvider;
import co.com.crediya.model.user.Rol;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import co.com.crediya.usecase.user.exception.UserValidationException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class LoginUseCaseTest {
    private static final String EMAIL = "ana@example.com";
    private static final String PASSWORD = "Clave1234";

    private final User storedUser = User.builder()
            .correoElectronico(EMAIL)
            .clave("hash:" + PASSWORD)
            .rol(Rol.CLIENTE)
            .build();
    private final UserRepository userRepository = new StubUserRepository(storedUser);
    private final PasswordHasher passwordHasher = new PasswordHasher() {
        @Override
        public String hash(String rawPassword) {
            return "hash:" + rawPassword;
        }

        @Override
        public boolean matches(String rawPassword, String hashedPassword) {
            return hash(rawPassword).equals(hashedPassword);
        }
    };
    private final TokenProvider tokenProvider = user -> new AccessToken("token-" + user.getRol(), 3600);
    private final LoginUseCase useCase = new LoginUseCase(userRepository, passwordHasher, tokenProvider);

    @Test
    void returnsTokenForValidCredentialsIgnoringEmailCase() {
        AccessToken token = useCase.execute("  ANA@Example.com ", PASSWORD);

        assertEquals("token-CLIENTE", token.token());
    }

    @Test
    void rejectsWrongPassword() {
        assertThrows(InvalidCredentialsException.class, () -> useCase.execute(EMAIL, "otra-clave"));
    }

    @Test
    void rejectsUnknownEmail() {
        assertThrows(
                InvalidCredentialsException.class, () -> useCase.execute("nadie@example.com", PASSWORD));
    }

    @Test
    void rejectsUsersWithoutPassword() {
        UserRepository repository = new StubUserRepository(storedUser.toBuilder().clave(null).build());
        LoginUseCase loginUseCase = new LoginUseCase(repository, passwordHasher, tokenProvider);

        assertThrows(InvalidCredentialsException.class, () -> loginUseCase.execute(EMAIL, PASSWORD));
    }

    @Test
    void rejectsBlankFields() {
        assertThrows(UserValidationException.class, () -> useCase.execute(" ", PASSWORD));
        assertThrows(UserValidationException.class, () -> useCase.execute(EMAIL, null));
    }

    private record StubUserRepository(User user) implements UserRepository {
        @Override
        public boolean existsByCorreoElectronico(String correoElectronico) {
            return false;
        }

        @Override
        public Optional<User> findByCorreoElectronico(String correoElectronico) {
            return Optional.of(user).filter(found -> found.getCorreoElectronico().equals(correoElectronico));
        }

        @Override
        public boolean existsByDocumentoIdentidad(String documentoIdentidad) {
            return false;
        }

        @Override
        public Optional<User> findByDocumentoIdentidad(String documentoIdentidad) {
            return Optional.empty();
        }

        @Override
        public User save(User user) {
            return user;
        }
    }
}
