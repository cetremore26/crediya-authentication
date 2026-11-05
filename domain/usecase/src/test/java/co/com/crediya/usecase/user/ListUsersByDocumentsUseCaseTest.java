package co.com.crediya.usecase.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import co.com.crediya.usecase.user.exception.UserValidationException;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class ListUsersByDocumentsUseCaseTest {
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
            return false;
        }

        @Override
        public Optional<User> findByDocumentoIdentidad(String documentoIdentidad) {
            return "111111".equals(documentoIdentidad) || "222222".equals(documentoIdentidad)
                    ? Optional.of(User.builder().documentoIdentidad(documentoIdentidad).build())
                    : Optional.empty();
        }

        @Override
        public User save(User user) {
            return user;
        }
    };
    private final ListUsersByDocumentsUseCase useCase = new ListUsersByDocumentsUseCase(repository);

    @Test
    void returnsKnownUsersWithoutDuplicatesAndSkipsUnknown() {
        List<User> users = useCase.execute(List.of("111111", " 222222 ", "111111", "999999"));

        assertEquals(2, users.size());
    }

    @Test
    void rejectsEmptyAndOversizedRequests() {
        List<String> tooMany = IntStream.range(0, 101).mapToObj(String::valueOf).toList();

        assertThrows(UserValidationException.class, () -> useCase.execute(null));
        assertThrows(UserValidationException.class, () -> useCase.execute(List.of()));
        assertThrows(UserValidationException.class, () -> useCase.execute(tooMany));
    }
}
