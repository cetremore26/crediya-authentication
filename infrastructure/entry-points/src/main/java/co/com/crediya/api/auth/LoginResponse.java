package co.com.crediya.api.auth;

import co.com.crediya.model.auth.AccessToken;
import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponse(
        String token,
        String tipo,
        @JsonProperty("expira_en_segundos") long expiraEnSegundos) {
    private static final String TOKEN_TYPE = "Bearer";

    public static LoginResponse from(AccessToken accessToken) {
        return new LoginResponse(
                accessToken.token(), TOKEN_TYPE, accessToken.expiresInSeconds());
    }
}
