package co.com.crediya.model.auth;

import co.com.crediya.model.user.Rol;
import java.util.UUID;

public record AuthenticatedUser(
        UUID userId, String correoElectronico, Rol rol, String documentoIdentidad) {
}
