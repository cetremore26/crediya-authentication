package co.com.crediya.model.autenticacion.exception;

public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException() {
        super("Correo o clave incorrectos.");
    }
}
