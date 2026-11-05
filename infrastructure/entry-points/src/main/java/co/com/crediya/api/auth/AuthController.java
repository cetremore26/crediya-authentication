package co.com.crediya.api.auth;

import co.com.crediya.api.ApiErrorResponse;
import co.com.crediya.model.auth.AccessToken;
import co.com.crediya.usecase.user.LoginUseCase;
import co.com.crediya.usecase.user.exception.UserValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Autenticación", description = "Inicio de sesión")
@RequestMapping("/api/v1/login")
public class AuthController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final LoginUseCase loginUseCase;

    public AuthController(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    @PostMapping
    @Operation(summary = "Iniciar sesión",
            description = "Autentica con correo y clave y devuelve un token JWT.")
    @ApiResponse(responseCode = "200", description = "Sesión iniciada")
    @ApiResponse(responseCode = "400", description = "Datos incompletos",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Correo o clave incorrectos",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        if (request == null) {
            throw new UserValidationException("El cuerpo de la solicitud es obligatorio.");
        }
        LOGGER.info("Solicitud de inicio de sesión recibida.");
        AccessToken accessToken =
                loginUseCase.execute(request.correoElectronico(), request.clave());
        LOGGER.info("Inicio de sesión exitoso.");
        return ResponseEntity.ok(LoginResponse.from(accessToken));
    }
}
