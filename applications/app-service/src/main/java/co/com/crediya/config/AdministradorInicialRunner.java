package co.com.crediya.config;

import co.com.crediya.model.usuario.Usuario;
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
 * Solo aporta los datos: comprobar si ya existe, normalizar, validar y guardar lo hace
 * {@link RegistrarUsuarioUseCase#registrarAdministradorInicial(Usuario)}.
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
        boolean creado = registrarUsuarioUseCase.registrarAdministradorInicial(Usuario.builder()
                .nombres(NOMBRES_ADMINISTRADOR)
                .apellidos(APELLIDOS_ADMINISTRADOR)
                .correoElectronico(adminEmail)
                .salarioBase(BigDecimal.ZERO)
                .clave(adminPassword)
                .build());
        LOGGER.info(creado ? "Administrador inicial creado." : "El administrador inicial ya existe.");
    }
}
