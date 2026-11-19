package co.com.crediya.model.autenticacion;

import co.com.crediya.model.usuario.Rol;
import java.util.UUID;

public record UsuarioAutenticado(
        UUID idUsuario, String correoElectronico, Rol rol, String documentoIdentidad) {

    /** El personal de CrediYa (ADMIN o ASESOR), a diferencia de los clientes. */
    public boolean esPersonal() {
        return rol == Rol.ADMIN || rol == Rol.ASESOR;
    }

    /** Si puede registrar un usuario con ese rol (ver {@link Rol#puedeAsignar(Rol)}). */
    public boolean puedeAsignarRol(Rol rolNuevo) {
        return rol != null && rol.puedeAsignar(rolNuevo);
    }

    /** El personal consulta a cualquier usuario; un CLIENTE solo a sí mismo. */
    public boolean puedeConsultarDocumento(String documento) {
        return esPersonal()
                || rol == Rol.CLIENTE && documento != null && documento.equals(documentoIdentidad);
    }
}
