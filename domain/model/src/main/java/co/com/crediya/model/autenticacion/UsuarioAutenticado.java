package co.com.crediya.model.autenticacion;

import co.com.crediya.model.usuario.Rol;
import java.util.UUID;

public record UsuarioAutenticado(
        UUID idUsuario, String correoElectronico, Rol rol, String documentoIdentidad) {

    /** El personal de CrediYa (ADMIN o ASESOR), a diferencia de los clientes. */
    public boolean esPersonal() {
        return rol == Rol.ADMIN || rol == Rol.ASESOR;
    }

    /** El personal consulta a cualquier usuario; un CLIENTE solo a sí mismo. */
    public boolean puedeConsultarDocumento(String documento) {
        return esPersonal()
                || rol == Rol.CLIENTE && documento != null && documento.equals(documentoIdentidad);
    }
}
