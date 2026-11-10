package co.com.crediya.usecase.user;

import co.com.crediya.model.auth.AccessToken;
import co.com.crediya.model.auth.exception.InvalidCredentialsException;
import co.com.crediya.model.auth.gateways.PasswordHasher;
import co.com.crediya.model.auth.gateways.TokenProvider;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.exception.UserValidationException;
import co.com.crediya.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;

/**
 * Inicia sesión. Solo orquesta: busca al usuario, delega la verificación de la clave en
 * {@link PasswordHasher} y la emisión del token en {@link TokenProvider}.
 */
@RequiredArgsConstructor
public class LoginUseCase {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;

    public AccessToken execute(String correoElectronico, String clave) {
        if (isBlank(correoElectronico) || isBlank(clave)) {
            throw new UserValidationException("El correo y la clave son obligatorios.");
        }

        User user = userRepository.findByCorreoElectronico(User.normalizeEmail(correoElectronico))
                .filter(found -> found.getClave() != null
                        && passwordHasher.matches(clave, found.getClave()))
                .orElseThrow(InvalidCredentialsException::new);

        return tokenProvider.generate(user);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
