package co.com.crediya.usecase.user;

import co.com.crediya.model.user.User;
import co.com.crediya.model.user.exception.UserAlreadyExistsException;
import co.com.crediya.model.user.gateways.UserRepository;
import co.com.crediya.usecase.user.exception.UserValidationException;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Pattern;

public class RegisterUserUseCase {
    private static final BigDecimal MINIMUM_SALARY = BigDecimal.ZERO;
    private static final BigDecimal MAXIMUM_SALARY = new BigDecimal("15000000");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UserRepository userRepository;

    public RegisterUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User execute(User user) {
        validate(user);

        String email = user.getCorreoElectronico().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByCorreoElectronico(email)) {
            throw new UserAlreadyExistsException();
        }

        User userToSave = user.toBuilder()
                .correoElectronico(email)
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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
