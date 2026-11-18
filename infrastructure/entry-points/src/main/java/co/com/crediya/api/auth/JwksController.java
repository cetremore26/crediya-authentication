package co.com.crediya.api.auth;

import co.com.crediya.usecase.auth.GetPublicKeySetUseCase;
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
public class JwksController {
    public static final String JWKS_PATH = "/.well-known/jwks.json";

    private final GetPublicKeySetUseCase getPublicKeySetUseCase;

    public JwksController(GetPublicKeySetUseCase getPublicKeySetUseCase) {
        this.getPublicKeySetUseCase = getPublicKeySetUseCase;
    }

    @GetMapping(value = JWKS_PATH, produces = "application/json")
    @Operation(summary = "Claves públicas de firma (JWKS)",
            description = "Claves RSA públicas con las que se verifican los tokens emitidos en el login.")
    @ApiResponse(responseCode = "200", description = "Conjunto de claves públicas")
    public ResponseEntity<Map<String, Object>> publicKeys() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(getPublicKeySetUseCase.execute());
    }
}
