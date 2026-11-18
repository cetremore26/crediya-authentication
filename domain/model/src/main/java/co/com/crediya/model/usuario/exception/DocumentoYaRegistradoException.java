package co.com.crediya.model.usuario.exception;

public class DocumentoYaRegistradoException extends UsuarioYaRegistradoException {
    public DocumentoYaRegistradoException() {
        super("Ya existe un usuario registrado con ese documento de identidad.");
    }
}
