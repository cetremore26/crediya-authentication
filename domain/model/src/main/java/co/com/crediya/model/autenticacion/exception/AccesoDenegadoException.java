package co.com.crediya.model.autenticacion.exception;

/** El usuario autenticado no tiene permiso para la operación que intenta. */
public class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException(String message) {
        super(message);
    }
}
