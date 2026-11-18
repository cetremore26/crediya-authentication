package co.com.crediya.config;

import co.com.crediya.model.user.Rol;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.exception.UserAlreadyExistsException;
import co.com.crediya.usecase.user.RegisterUserUseCase;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Creates the first administrator so that the rest of the users can be registered.
 * It only supplies the data: normalizing, validating and checking for duplicates is done by
 * {@link RegisterUserUseCase}, as for any other user.
 */
@Component
public class AdminSeeder implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminSeeder.class);
    private static final String ADMIN_NAMES = "Administrador";
    private static final String ADMIN_LAST_NAMES = "CrediYa";

    private final RegisterUserUseCase registerUserUseCase;
    private final String adminEmail;
    private final String adminPassword;

    public AdminSeeder(
            RegisterUserUseCase registerUserUseCase,
            @Value("${crediya.admin.email}") String adminEmail,
            @Value("${crediya.admin.password}") String adminPassword) {
        this.registerUserUseCase = registerUserUseCase;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            registerUserUseCase.execute(User.builder()
                    .nombres(ADMIN_NAMES)
                    .apellidos(ADMIN_LAST_NAMES)
                    .correoElectronico(adminEmail)
                    .salarioBase(BigDecimal.ZERO)
                    .clave(adminPassword)
                    .rol(Rol.ADMIN)
                    .build());
            LOGGER.info("Administrador inicial creado.");
        } catch (UserAlreadyExistsException exception) {
            LOGGER.info("El administrador inicial ya existe.");
        }
    }
}
