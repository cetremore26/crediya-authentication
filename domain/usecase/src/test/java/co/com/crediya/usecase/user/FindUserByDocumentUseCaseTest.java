package co.com.crediya.usecase.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.com.crediya.model.user.User;
import co.com.crediya.model.user.exception.UserNotFoundException;
import co.com.crediya.model.user.gateways.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class FindUserByDocumentUseCaseTest {
    private static final String DOCUMENTO = "1020304050";

    private final UserRepository repository = new UserRepository() {
        @Override
        public boolean existsByCorreoElectronico(String correoElectronico) {
            return false;
        }

        @Override
        public Optional<User> findByCorreoElectronico(String correoElectronico) {
            return Optional.empty();
        }

        @Override
        public boolean existsByDocumentoIdentidad(String documentoIdentidad) {
            return DOCUMENTO.equals(documentoIdentidad);
        }

        @Override
        public Optional<User> findByDocumentoIdentidad(String documentoIdentidad) {
            return DOCUMENTO.equals(documentoIdentidad)
                    ? Optional.of(User.builder().documentoIdentidad(DOCUMENTO).build())
                    : Optional.empty();
        }

        @Override
        public User save(User user) {
            return user;
        }
    };
    private final FindUserByDocumentUseCase useCase = new FindUserByDocumentUseCase(repository);

    @Test
    void returnsUserWhenDocumentExists() {
        assertEquals(DOCUMENTO, useCase.execute(DOCUMENTO).getDocumentoIdentidad());
    }

    @Test
    void throwsWhenDocumentDoesNotExist() {
        assertThrows(UserNotFoundException.class, () -> useCase.execute("999999"));
    }
}
