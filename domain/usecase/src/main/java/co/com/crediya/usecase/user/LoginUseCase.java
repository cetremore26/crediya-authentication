package co.com.crediya.usecase.user;

import co.com.crediya.model.auth.AccessToken;
import co.com.crediya.model.auth.exception.InvalidCredentialsException;
import co.com.crediya.model.auth.gateways.PasswordHasher;
import co.com.crediya.model.auth.gateways.TokenProvider;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.exception.UserValidationException;
import co.com.crediya.model.user.gateways.UserRepository;
import java.util.Optional;

/**
 * Inicia sesión. Solo orquesta: busca al usuario, delega la verificación de la clave en
 * {@link PasswordHasher} y la emisión del token en {@link TokenProvider}.
 *
 * <p>La clave se verifica siempre, también cuando el correo no existe (contra un hash de
 * referencia): así la respuesta tarda lo mismo en ambos casos y no revela qué correos están
 * registrados.
 */
public class LoginUseCase {
    private static final String CLAVE_DE_REFERENCIA = "clave-de-referencia-sin-usuario";

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;
    private final String hashDeReferencia;

    public LoginUseCase(
            UserRepository userRepository, PasswordHasher passwordHasher, TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
        this.hashDeReferencia = passwordHasher.hash(CLAVE_DE_REFERENCIA);
    }

    public AccessToken execute(String correoElectronico, String clave) {
        if (isBlank(correoElectronico) || isBlank(clave)) {
            throw new UserValidationException("El correo y la clave son obligatorios.");
        }

        Optional<User> user = userRepository
                .findByCorreoElectronico(User.normalizeEmail(correoElectronico))
                .filter(found -> found.getClave() != null);
        boolean claveCorrecta =
                passwordHasher.matches(clave, user.map(User::getClave).orElse(hashDeReferencia));

        if (user.isEmpty() || !claveCorrecta) {
            throw new InvalidCredentialsException();
        }
        return tokenProvider.generate(user.get());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
