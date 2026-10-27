package co.com.crediya.model.user.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException() {
        super("Ya existe un usuario registrado con ese correo electrónico.");
    }
}
