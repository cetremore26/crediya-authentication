package co.com.crediya.model.auth;

import co.com.crediya.model.user.Rol;
import java.util.UUID;

public record AuthenticatedUser(
        UUID userId, String correoElectronico, Rol rol, String documentoIdentidad) {

    /** El personal (ADMIN, ASESOR) consulta a cualquier usuario; un CLIENTE solo a sí mismo. */
    public boolean puedeConsultarDocumento(String documento) {
        return switch (rol) {
            case ADMIN, ASESOR -> true;
            case CLIENTE -> documento != null && documento.equals(documentoIdentidad);
        };
    }
}
