package co.com.crediya.api.user;

import co.com.crediya.usecase.user.ListUsersByDocumentsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
@RequestMapping("/api/v1/usuarios")
public class UserLookupController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserLookupController.class);

    private final ListUsersByDocumentsUseCase listUsersByDocumentsUseCase;

    public UserLookupController(ListUsersByDocumentsUseCase listUsersByDocumentsUseCase) {
        this.listUsersByDocumentsUseCase = listUsersByDocumentsUseCase;
    }

    @GetMapping
    @Operation(summary = "Consultar usuarios por documentos",
            description = "Solo ADMIN/ASESOR. Devuelve los usuarios que existan para los documentos indicados.")
    @ApiResponse(responseCode = "200", description = "Usuarios encontrados")
    @ApiResponse(responseCode = "400", description = "Lista de documentos vacía o demasiado grande")
    public ResponseEntity<List<UserDetailResponse>> findByDocuments(
            @RequestParam(value = "documentos", required = false) List<String> documentos) {
        LOGGER.info("Consulta de usuarios por lote de documentos.");
        List<UserDetailResponse> users = listUsersByDocumentsUseCase.execute(documentos).stream()
                .map(UserDetailResponse::from)
                .toList();
        return ResponseEntity.ok(users);
    }
}
