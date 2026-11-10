package co.com.crediya.api.user;

import co.com.crediya.api.ApiErrorResponse;
import co.com.crediya.usecase.user.FindUserByDocumentUseCase;
import co.com.crediya.usecase.user.ListUsersByDocumentsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
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
public class UserLookupController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserLookupController.class);

    private final FindUserByDocumentUseCase findUserByDocumentUseCase;
    private final ListUsersByDocumentsUseCase listUsersByDocumentsUseCase;

    public UserLookupController(
            FindUserByDocumentUseCase findUserByDocumentUseCase,
            ListUsersByDocumentsUseCase listUsersByDocumentsUseCase) {
        this.findUserByDocumentUseCase = findUserByDocumentUseCase;
        this.listUsersByDocumentsUseCase = listUsersByDocumentsUseCase;
    }

    @GetMapping("/documento/{documento}")
    @Operation(summary = "Consultar un usuario por documento de identidad",
            description = "Usado por otros microservicios para verificar la existencia de un cliente.")
    @ApiResponse(responseCode = "200", description = "Usuario encontrado")
    @ApiResponse(responseCode = "404", description = "No existe un usuario con ese documento",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<UserDocumentResponse> findByDocument(
            @PathVariable("documento") String documento) {
        LOGGER.info("Consulta de usuario por documento de identidad.");
        return ResponseEntity.ok(UserDocumentResponse.from(findUserByDocumentUseCase.execute(documento)));
    }

    @GetMapping
    @Operation(summary = "Consultar usuarios por documentos",
            description = "Solo ADMIN/ASESOR. Devuelve los usuarios que existan para los documentos indicados.")
    @ApiResponse(responseCode = "200", description = "Usuarios encontrados")
    @ApiResponse(responseCode = "400", description = "Lista de documentos vacía o demasiado grande",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<List<UserDetailResponse>> findByDocuments(
            @RequestParam(value = "documentos", required = false) List<String> documentos) {
        LOGGER.info("Consulta de usuarios por lote de documentos.");
        List<UserDetailResponse> users = listUsersByDocumentsUseCase.execute(documentos).stream()
                .map(UserDetailResponse::from)
                .toList();
        return ResponseEntity.ok(users);
    }
}
