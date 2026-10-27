package co.com.crediya.usecase.user;

import co.com.crediya.model.auth.gateways.PasswordHasher;
import co.com.crediya.model.user.Rol;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.exception.UserAlreadyExistsException;
import co.com.crediya.model.user.gateways.UserRepository;
import co.com.crediya.usecase.user.exception.UserValidationException;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Pattern;

public class RegisterUserUseCase {
    private static final int MINIMUM_PASSWORD_LENGTH = 8;
    private static final int MAXIMUM_PASSWORD_LENGTH = 72;
    private static final BigDecimal MINIMUM_SALARY = BigDecimal.ZERO;
    private static final BigDecimal MAXIMUM_SALARY = new BigDecimal("15000000");
    private static final Pattern DOCUMENTO_PATTERN = Pattern.compile("^\\d{5,15}$");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public RegisterUserUseCase(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    public User execute(User user) {
        validate(user);

        String email = user.getCorreoElectronico().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByCorreoElectronico(email)) {
            throw new UserAlreadyExistsException();
        }

        String documento = normalizeDocumento(user.getDocumentoIdentidad());
        if (documento != null && userRepository.existsByDocumentoIdentidad(documento)) {
            throw new UserAlreadyExistsException();
        }

        User userToSave = user.toBuilder()
                .documentoIdentidad(documento)
                .correoElectronico(email)
                .rol(resolveRol(user))
                .clave(passwordHasher.hash(user.getClave()))
                .build();
        return userRepository.save(userToSave);
    }

    private void validate(User user) {
        if (user == null) {
            throw new UserValidationException("El usuario es obligatorio.");
        }
        if (isBlank(user.getNombres())) {
            throw new UserValidationException("Los nombres son obligatorios.");
        }
        if (isBlank(user.getApellidos())) {
            throw new UserValidationException("Los apellidos son obligatorios.");
        }

        String documento = normalizeDocumento(user.getDocumentoIdentidad());
        if (documento != null && !DOCUMENTO_PATTERN.matcher(documento).matches()) {
            throw new UserValidationException(
                    "El documento de identidad debe contener entre 5 y 15 dígitos.");
        }

        if (resolveRol(user) == Rol.CLIENTE && documento == null) {
            throw new UserValidationException(
                    "El documento de identidad es obligatorio para los clientes.");
        }

        String clave = user.getClave();
        if (clave == null
                || clave.length() < MINIMUM_PASSWORD_LENGTH
                || clave.length() > MAXIMUM_PASSWORD_LENGTH) {
            throw new UserValidationException(
                    "La clave debe tener entre 8 y 72 caracteres.");
        }

        String email = user.getCorreoElectronico();
        if (isBlank(email) || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new UserValidationException("El correo electrónico no es válido.");
        }

        BigDecimal salary = user.getSalarioBase();
        if (salary == null
                || salary.compareTo(MINIMUM_SALARY) < 0
                || salary.compareTo(MAXIMUM_SALARY) > 0) {
            throw new UserValidationException(
                    "El salario base debe estar entre 0 y 15000000.");
        }
    }

    private Rol resolveRol(User user) {
        return user.getRol() == null ? Rol.CLIENTE : user.getRol();
    }

    private String normalizeDocumento(String documento) {
        return isBlank(documento) ? null : documento.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
