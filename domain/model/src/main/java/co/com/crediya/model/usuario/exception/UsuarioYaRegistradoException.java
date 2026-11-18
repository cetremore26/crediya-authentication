package co.com.crediya.model.usuario.exception;

public class UsuarioYaRegistradoException extends RuntimeException {
    public UsuarioYaRegistradoException() {
        super("Ya existe un usuario registrado con ese correo electrónico.");
    }

    protected UsuarioYaRegistradoException(String message) {
        super(message);
    }
}
