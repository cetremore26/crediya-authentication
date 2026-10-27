package co.com.crediya.api.user;

import co.com.crediya.usecase.user.RegisterUserUseCase;
import co.com.crediya.usecase.user.exception.UserValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import co.com.crediya.api.ApiErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
@RequestMapping("/api/v1/usuarios")
public class UserController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final RegisterUserUseCase registerUserUseCase;

    public UserController(RegisterUserUseCase registerUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
    }

    @PostMapping
    @Operation(summary = "Registrar un nuevo usuario",
            description = "Registra un solicitante con sus datos personales básicos.")
    @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "El correo electrónico ya está registrado",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<UserResponse> register(@RequestBody UserRequest request) {
        if (request == null) {
            throw new UserValidationException("El cuerpo de la solicitud es obligatorio.");
        }
        LOGGER.info("Solicitud de registro de usuario recibida.");
        registerUserUseCase.execute(request.toDomain());
        LOGGER.info("Usuario registrado exitosamente.");
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.registered());
    }
}
