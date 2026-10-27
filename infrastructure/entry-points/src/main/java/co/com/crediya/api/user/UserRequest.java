package co.com.crediya.api.user;

import co.com.crediya.model.user.Rol;
import co.com.crediya.model.user.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UserRequest(
        @JsonProperty("documento_identidad") String documentoIdentidad,
        String nombres,
        String apellidos,
        @JsonProperty("fecha_nacimiento") LocalDate fechaNacimiento,
        String direccion,
        String telefono,
        @JsonProperty("correo_electronico") String correoElectronico,
        @JsonProperty("salario_base") BigDecimal salarioBase,
        String clave,
        Rol rol) {

    public User toDomain() {
        return User.builder()
                .documentoIdentidad(documentoIdentidad)
                .nombres(nombres)
                .apellidos(apellidos)
                .fechaNacimiento(fechaNacimiento)
                .direccion(direccion)
                .telefono(telefono)
                .correoElectronico(correoElectronico)
                .salarioBase(salarioBase)
                .clave(clave)
                .rol(rol)
                .build();
    }
}
