package co.com.crediya.model.auth.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Correo o clave incorrectos.");
    }
}
