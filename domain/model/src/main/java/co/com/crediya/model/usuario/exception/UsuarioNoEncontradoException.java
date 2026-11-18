package co.com.crediya.model.usuario.exception;

public class UsuarioNoEncontradoException extends RuntimeException {
    public UsuarioNoEncontradoException() {
        super("No existe un usuario registrado con ese documento de identidad.");
    }
}
