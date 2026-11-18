package co.com.crediya.model.user.gateways;

import co.com.crediya.model.user.User;
import co.com.crediya.model.user.exception.UserAlreadyExistsException;
import java.util.Optional;

/**
 * Persistencia de usuarios. Solo guarda y consulta: recibe los usuarios ya normalizados y
 * validados por el dominio ({@link User#normalized()}, {@link User#validate()}).
 */
public interface UserRepository {
    boolean existsByCorreoElectronico(String correoElectronico);

    Optional<User> findByCorreoElectronico(String correoElectronico);

    boolean existsByDocumentoIdentidad(String documentoIdentidad);

    Optional<User> findByDocumentoIdentidad(String documentoIdentidad);

    /**
     * Persists a user or throws if the email or the document is already registered.
     *
     * @throws UserAlreadyExistsException when another user has the same email or document
     */
    User save(User user);
}
