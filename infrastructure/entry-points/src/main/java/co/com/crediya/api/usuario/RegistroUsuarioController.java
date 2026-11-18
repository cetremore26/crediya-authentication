package co.com.crediya.api.usuario;

import co.com.crediya.api.ErrorApiResponse;
import co.com.crediya.model.autenticacion.UsuarioAutenticado;
import co.com.crediya.usecase.usuario.RegistrarUsuarioUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Registro de usuarios. Las consultas están en {@link ConsultaUsuarioController}. */
@RestController
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
@RequestMapping("/api/v1/usuarios")
public class RegistroUsuarioController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistroUsuarioController.class);

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;

    public RegistroUsuarioController(RegistrarUsuarioUseCase registrarUsuarioUseCase) {
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
    }

    @PostMapping
    @Operation(summary = "Registrar un nuevo usuario",
            description = "Registra un solicitante con sus datos personales básicos.")
    @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos",
            content = @Content(schema = @Schema(implementation = ErrorApiResponse.class)))
    @ApiResponse(responseCode = "403",
            description = "El rol de quien registra no puede asignar el rol solicitado "
                    + "(un ASESOR solo registra clientes)",
            content = @Content(schema = @Schema(implementation = ErrorApiResponse.class)))
    @ApiResponse(responseCode = "409", description = "El correo o el documento ya están registrados",
            content = @Content(schema = @Schema(implementation = ErrorApiResponse.class)))
    public ResponseEntity<UsuarioResponse> registrar(
            @RequestBody UsuarioRequest request,
            @AuthenticationPrincipal UsuarioAutenticado solicitante) {
        LOGGER.info("Solicitud de registro de usuario recibida.");
        registrarUsuarioUseCase.execute(request.aDominio(), solicitante.rol());
        LOGGER.info("Usuario registrado exitosamente.");
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.registrado());
    }
}
