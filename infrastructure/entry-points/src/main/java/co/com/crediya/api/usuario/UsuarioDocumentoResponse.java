package co.com.crediya.api.usuario;

import co.com.crediya.model.usuario.Usuario;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UsuarioDocumentoResponse(
        @JsonProperty("documento_identidad") String documentoIdentidad,
        String nombres,
        String apellidos) {

    public static UsuarioDocumentoResponse desde(Usuario usuario) {
        return new UsuarioDocumentoResponse(
                usuario.getDocumentoIdentidad(), usuario.getNombres(), usuario.getApellidos());
    }
}
