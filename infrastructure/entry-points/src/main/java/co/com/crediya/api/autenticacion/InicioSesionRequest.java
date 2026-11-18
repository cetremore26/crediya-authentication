package co.com.crediya.api.autenticacion;

import com.fasterxml.jackson.annotation.JsonProperty;

public record InicioSesionRequest(
        @JsonProperty("correo_electronico") String correoElectronico,
        String clave) {
}
