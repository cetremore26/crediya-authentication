package co.com.crediya.model.user.gateways;

import co.com.crediya.model.user.User;
import co.com.crediya.model.user.exception.UserAlreadyExistsException;

public interface UserRepository {
    boolean existsByCorreoElectronico(String correoElectronico);

    /**
     * Persists a user or throws if the email is already registered.
     *
     * @throws UserAlreadyExistsException when another user has the same email
     */
    User save(User user);
}
