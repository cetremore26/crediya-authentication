package co.com.crediya.api.autenticacion;

import co.com.crediya.api.ErrorApiResponse;
import co.com.crediya.model.autenticacion.TokenAcceso;
import co.com.crediya.usecase.usuario.IniciarSesionUseCase;
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
public class InicioSesionController {
    private static final Logger LOGGER = LoggerFactory.getLogger(InicioSesionController.class);

    private final IniciarSesionUseCase iniciarSesionUseCase;

    public InicioSesionController(IniciarSesionUseCase iniciarSesionUseCase) {
        this.iniciarSesionUseCase = iniciarSesionUseCase;
    }

    @PostMapping
    @Operation(summary = "Iniciar sesión",
            description = "Autentica con correo y clave y devuelve un token JWT.")
    @ApiResponse(responseCode = "200", description = "Sesión iniciada")
    @ApiResponse(responseCode = "400", description = "Datos incompletos",
            content = @Content(schema = @Schema(implementation = ErrorApiResponse.class)))
    @ApiResponse(responseCode = "401", description = "Correo o clave incorrectos",
            content = @Content(schema = @Schema(implementation = ErrorApiResponse.class)))
    public ResponseEntity<InicioSesionResponse> iniciarSesion(@RequestBody InicioSesionRequest request) {
        LOGGER.info("Solicitud de inicio de sesión recibida.");
        TokenAcceso tokenAcceso =
                iniciarSesionUseCase.execute(request.correoElectronico(), request.clave());
        LOGGER.info("Inicio de sesión exitoso.");
        return ResponseEntity.ok(InicioSesionResponse.desde(tokenAcceso));
    }
}
