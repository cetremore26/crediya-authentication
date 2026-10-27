package co.com.crediya.config;

import co.com.crediya.model.user.Rol;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import co.com.crediya.usecase.user.RegisterUserUseCase;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** Creates the first administrator so that the rest of the users can be registered. */
@Component
public class AdminSeeder implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminSeeder.class);
    private static final String ADMIN_NAMES = "Administrador";
    private static final String ADMIN_LAST_NAMES = "CrediYa";

    private final UserRepository userRepository;
    private final RegisterUserUseCase registerUserUseCase;
    private final String adminEmail;
    private final String adminPassword;

    public AdminSeeder(
            UserRepository userRepository,
            RegisterUserUseCase registerUserUseCase,
            @Value("${crediya.admin.email}") String adminEmail,
            @Value("${crediya.admin.password}") String adminPassword) {
        this.userRepository = userRepository;
        this.registerUserUseCase = registerUserUseCase;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByCorreoElectronico(adminEmail.toLowerCase())) {
            return;
        }
        registerUserUseCase.execute(User.builder()
                .nombres(ADMIN_NAMES)
                .apellidos(ADMIN_LAST_NAMES)
                .correoElectronico(adminEmail)
                .salarioBase(BigDecimal.ZERO)
                .clave(adminPassword)
                .rol(Rol.ADMIN)
                .build());
        LOGGER.info("Administrador inicial creado.");
    }
}
