package co.com.crediya.api.autenticacion;

import co.com.crediya.model.autenticacion.TokenAcceso;
import com.fasterxml.jackson.annotation.JsonProperty;

public record InicioSesionResponse(
        String token,
        String tipo,
        @JsonProperty("expira_en_segundos") long expiraEnSegundos) {
    private static final String TIPO_TOKEN = "Bearer";

    public static InicioSesionResponse desde(TokenAcceso tokenAcceso) {
        return new InicioSesionResponse(
                tokenAcceso.token(), TIPO_TOKEN, tokenAcceso.expiraEnSegundos());
    }
}
