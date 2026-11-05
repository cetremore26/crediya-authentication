package co.com.crediya.api.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginRequest(
        @JsonProperty("correo_electronico") String correoElectronico,
        String clave) {
}
