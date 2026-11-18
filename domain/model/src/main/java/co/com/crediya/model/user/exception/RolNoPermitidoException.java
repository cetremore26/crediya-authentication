package co.com.crediya.model.user.exception;

import co.com.crediya.model.user.Rol;

/** Quien registra no tiene permiso para crear usuarios con el rol solicitado. */
public class RolNoPermitidoException extends RuntimeException {
    public RolNoPermitidoException(Rol rolSolicitado) {
        super("No tiene permisos para registrar usuarios con el rol " + rolSolicitado + ".");
    }
}
