package co.com.crediya.usecase.user;

import co.com.crediya.model.auth.gateways.PasswordHasher;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.exception.DocumentAlreadyRegisteredException;
import co.com.crediya.model.user.exception.UserAlreadyExistsException;
import co.com.crediya.model.user.exception.UserValidationException;
import co.com.crediya.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;

/**
 * Registra un usuario nuevo. Solo orquesta el flujo: las reglas de qué es un usuario
 * válido viven en {@link User}.
 */
@RequiredArgsConstructor
public class RegisterUserUseCase {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public User execute(User user) {
        if (user == null) {
            throw new UserValidationException("El usuario es obligatorio.");
        }

        User candidate = user.normalized();
        candidate.validate();
        ensureIsNotRegistered(candidate);

        return userRepository.save(candidate.toBuilder()
                .clave(passwordHasher.hash(candidate.getClave()))
                .build());
    }

    private void ensureIsNotRegistered(User user) {
        if (userRepository.existsByCorreoElectronico(user.getCorreoElectronico())) {
            throw new UserAlreadyExistsException();
        }
        String documento = user.getDocumentoIdentidad();
        if (documento != null && userRepository.existsByDocumentoIdentidad(documento)) {
            throw new DocumentAlreadyRegisteredException();
        }
    }
}
