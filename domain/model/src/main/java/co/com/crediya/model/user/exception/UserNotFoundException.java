package co.com.crediya.model.user.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("No existe un usuario registrado con ese documento de identidad.");
    }
}
