package co.com.crediya.model.user.gateways;

import co.com.crediya.model.user.User;
import co.com.crediya.model.user.exception.UserAlreadyExistsException;
import java.util.Optional;

public interface UserRepository {
    boolean existsByCorreoElectronico(String correoElectronico);

    boolean existsByDocumentoIdentidad(String documentoIdentidad);

    Optional<User> findByDocumentoIdentidad(String documentoIdentidad);

    /**
     * Persists a user or throws if the email is already registered.
     *
     * @throws UserAlreadyExistsException when another user has the same email
     */
    User save(User user);
}
