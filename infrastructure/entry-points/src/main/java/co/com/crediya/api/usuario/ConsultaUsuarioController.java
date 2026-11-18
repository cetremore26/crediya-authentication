package co.com.crediya.api.usuario;

import co.com.crediya.api.ErrorApiResponse;
import co.com.crediya.model.autenticacion.UsuarioAutenticado;
import co.com.crediya.usecase.usuario.ConsultarUsuarioPorDocumentoUseCase;
import co.com.crediya.usecase.usuario.ConsultarUsuariosPorDocumentosUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Consultas de usuarios. Cada respuesta expone solo los campos que su consumidor necesita
 * (nunca la clave): esa decisión es de esta capa, no del repositorio.
 */
@RestController
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
@RequestMapping("/api/v1/usuarios")
public class ConsultaUsuarioController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsultaUsuarioController.class);

    private final ConsultarUsuarioPorDocumentoUseCase consultarUsuarioPorDocumentoUseCase;
    private final ConsultarUsuariosPorDocumentosUseCase consultarUsuariosPorDocumentosUseCase;

    public ConsultaUsuarioController(
            ConsultarUsuarioPorDocumentoUseCase consultarUsuarioPorDocumentoUseCase,
            ConsultarUsuariosPorDocumentosUseCase consultarUsuariosPorDocumentosUseCase) {
        this.consultarUsuarioPorDocumentoUseCase = consultarUsuarioPorDocumentoUseCase;
        this.consultarUsuariosPorDocumentosUseCase = consultarUsuariosPorDocumentosUseCase;
    }

    @GetMapping("/documento/{documento}")
    @Operation(summary = "Consultar un usuario por documento de identidad",
            description = "Usado por otros microservicios para verificar la existencia de un cliente. "
                    + "ADMIN y ASESOR consultan cualquier documento; un CLIENTE solo el suyo.")
    @ApiResponse(responseCode = "200", description = "Usuario encontrado")
    @ApiResponse(responseCode = "403", description = "Un CLIENTE consultando otro documento",
            content = @Content(schema = @Schema(implementation = ErrorApiResponse.class)))
    @ApiResponse(responseCode = "404", description = "No existe un usuario con ese documento",
            content = @Content(schema = @Schema(implementation = ErrorApiResponse.class)))
    public ResponseEntity<UsuarioDocumentoResponse> consultarPorDocumento(
            @PathVariable("documento") String documento,
            @AuthenticationPrincipal UsuarioAutenticado solicitante) {
        LOGGER.info("Consulta de usuario por documento de identidad.");
        return ResponseEntity.ok(UsuarioDocumentoResponse.desde(
                consultarUsuarioPorDocumentoUseCase.execute(documento, solicitante)));
    }

    @GetMapping
    @Operation(summary = "Consultar usuarios por documentos",
            description = "Solo ADMIN/ASESOR. Devuelve los usuarios que existan para los documentos indicados.")
    @ApiResponse(responseCode = "200", description = "Usuarios encontrados")
    @ApiResponse(responseCode = "400", description = "Lista de documentos vacía o demasiado grande",
            content = @Content(schema = @Schema(implementation = ErrorApiResponse.class)))
    @ApiResponse(responseCode = "403", description = "Solo ADMIN o ASESOR",
            content = @Content(schema = @Schema(implementation = ErrorApiResponse.class)))
    public ResponseEntity<List<UsuarioDetalleResponse>> consultarPorDocumentos(
            @RequestParam(value = "documentos", required = false) List<String> documentos,
            @AuthenticationPrincipal UsuarioAutenticado solicitante) {
        LOGGER.info("Consulta de usuarios por lote de documentos.");
        List<UsuarioDetalleResponse> usuarios = consultarUsuariosPorDocumentosUseCase.execute(documentos, solicitante).stream()
                .map(UsuarioDetalleResponse::desde)
                .toList();
        return ResponseEntity.ok(usuarios);
    }
}
