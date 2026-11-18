package co.com.crediya.usecase.user;

import co.com.crediya.model.auth.gateways.PasswordHasher;
import co.com.crediya.model.user.Rol;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.exception.DocumentAlreadyRegisteredException;
import co.com.crediya.model.user.exception.RolNoPermitidoException;
import co.com.crediya.model.user.exception.UserAlreadyExistsException;
import co.com.crediya.model.user.exception.UserValidationException;
import co.com.crediya.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;

/**
 * Registra un usuario nuevo. Solo orquesta el flujo: las reglas de qué es un usuario
 * válido viven en {@link User} y las de quién puede asignar cada rol en {@link Rol}.
 */
@RequiredArgsConstructor
public class RegisterUserUseCase {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    /**
     * @param user datos del usuario a registrar
     * @param rolSolicitante rol de quien hace el registro
     * @throws RolNoPermitidoException cuando ese rol no puede asignar el rol del usuario nuevo
     */
    public User execute(User user, Rol rolSolicitante) {
        if (user == null) {
            throw new UserValidationException("El usuario es obligatorio.");
        }

        User candidate = user.normalized();
        ensureCanAssignRole(rolSolicitante, candidate.getRol());
        candidate.validate();
        ensureIsNotRegistered(candidate);

        return userRepository.save(candidate.toBuilder()
                .clave(passwordHasher.hash(candidate.getClave()))
                .build());
    }

    private static void ensureCanAssignRole(Rol rolSolicitante, Rol rolNuevo) {
        if (rolSolicitante == null || !rolSolicitante.puedeAsignar(rolNuevo)) {
            throw new RolNoPermitidoException(rolNuevo);
        }
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
