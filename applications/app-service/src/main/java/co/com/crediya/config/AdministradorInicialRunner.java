package co.com.crediya.config;

import co.com.crediya.model.usuario.Rol;
import co.com.crediya.model.usuario.Usuario;
import co.com.crediya.model.usuario.exception.UsuarioYaRegistradoException;
import co.com.crediya.usecase.usuario.RegistrarUsuarioUseCase;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Crea el primer administrador para que se puedan registrar los demás usuarios.
 * Solo aporta los datos: normalizar, validar y revisar duplicados lo hace
 * {@link RegistrarUsuarioUseCase}, como con cualquier usuario. El arranque actúa con autoridad
 * de administrador, porque todavía no existe nadie más que pueda registrarlo.
 */
@Component
public class AdministradorInicialRunner implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdministradorInicialRunner.class);
    private static final String NOMBRES_ADMINISTRADOR = "Administrador";
    private static final String APELLIDOS_ADMINISTRADOR = "CrediYa";

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final String adminEmail;
    private final String adminPassword;

    public AdministradorInicialRunner(
            RegistrarUsuarioUseCase registrarUsuarioUseCase,
            @Value("${crediya.admin.email}") String adminEmail,
            @Value("${crediya.admin.password}") String adminPassword) {
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            registrarUsuarioUseCase.execute(Usuario.builder()
                    .nombres(NOMBRES_ADMINISTRADOR)
                    .apellidos(APELLIDOS_ADMINISTRADOR)
                    .correoElectronico(adminEmail)
                    .salarioBase(BigDecimal.ZERO)
                    .clave(adminPassword)
                    .rol(Rol.ADMIN)
                    .build(), Rol.ADMIN);
            LOGGER.info("Administrador inicial creado.");
        } catch (UsuarioYaRegistradoException exception) {
            LOGGER.info("El administrador inicial ya existe.");
        }
    }
}
