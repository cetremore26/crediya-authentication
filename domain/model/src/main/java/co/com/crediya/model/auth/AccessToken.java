package co.com.crediya.model.auth;

public record AccessToken(String token, long expiresInSeconds) {
}
