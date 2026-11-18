package co.com.crediya.api.autenticacion;

import co.com.crediya.usecase.autenticacion.ObtenerClavesPublicasUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint estándar JWKS: otros servicios obtienen aquí la clave pública para verificar tokens. */
@RestController
@Tag(name = "Autenticación", description = "Inicio de sesión")
public class ClavesPublicasController {
    public static final String RUTA_JWKS = "/.well-known/jwks.json";

    private final ObtenerClavesPublicasUseCase obtenerClavesPublicasUseCase;

    public ClavesPublicasController(ObtenerClavesPublicasUseCase obtenerClavesPublicasUseCase) {
        this.obtenerClavesPublicasUseCase = obtenerClavesPublicasUseCase;
    }

    @GetMapping(value = RUTA_JWKS, produces = "application/json")
    @Operation(summary = "Claves públicas de firma (JWKS)",
            description = "Claves RSA públicas con las que se verifican los tokens emitidos al iniciar sesión.")
    @ApiResponse(responseCode = "200", description = "Conjunto de claves públicas")
    public ResponseEntity<Map<String, Object>> clavesPublicas() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(obtenerClavesPublicasUseCase.execute());
    }
}
