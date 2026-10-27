package co.com.crediya.api.user;

import co.com.crediya.model.user.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record UserDetailResponse(
        @JsonProperty("documento_identidad") String documentoIdentidad,
        String nombres,
        String apellidos,
        @JsonProperty("correo_electronico") String correoElectronico,
        @JsonProperty("salario_base") BigDecimal salarioBase) {

    public static UserDetailResponse from(User user) {
        return new UserDetailResponse(
                user.getDocumentoIdentidad(),
                user.getNombres(),
                user.getApellidos(),
                user.getCorreoElectronico(),
                user.getSalarioBase());
    }
}
