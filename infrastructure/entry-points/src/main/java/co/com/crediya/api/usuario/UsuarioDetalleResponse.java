package co.com.crediya.api.usuario;

import co.com.crediya.model.usuario.Usuario;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record UsuarioDetalleResponse(
        @JsonProperty("documento_identidad") String documentoIdentidad,
        String nombres,
        String apellidos,
        @JsonProperty("correo_electronico") String correoElectronico,
        @JsonProperty("salario_base") BigDecimal salarioBase) {

    public static UsuarioDetalleResponse desde(Usuario usuario) {
        return new UsuarioDetalleResponse(
                usuario.getDocumentoIdentidad(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getCorreoElectronico(),
                usuario.getSalarioBase());
    }
}
