package co.com.crediya.usecase.user;

import co.com.crediya.model.auth.AccessToken;
import co.com.crediya.model.auth.exception.InvalidCredentialsException;
import co.com.crediya.model.auth.gateways.PasswordHasher;
import co.com.crediya.model.auth.gateways.TokenProvider;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import co.com.crediya.usecase.user.exception.UserValidationException;
import java.util.Locale;

public class LoginUseCase {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;

    public LoginUseCase(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
    }

    public AccessToken execute(String correoElectronico, String clave) {
        if (isBlank(correoElectronico) || isBlank(clave)) {
            throw new UserValidationException("El correo y la clave son obligatorios.");
        }

        String email = correoElectronico.trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByCorreoElectronico(email)
                .filter(found -> found.getClave() != null
                        && passwordHasher.matches(clave, found.getClave()))
                .orElseThrow(InvalidCredentialsException::new);

        return tokenProvider.generate(user);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
