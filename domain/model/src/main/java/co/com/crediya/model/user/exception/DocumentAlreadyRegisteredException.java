package co.com.crediya.model.user.exception;

public class DocumentAlreadyRegisteredException extends UserAlreadyExistsException {
    public DocumentAlreadyRegisteredException() {
        super("Ya existe un usuario registrado con ese documento de identidad.");
    }
}
