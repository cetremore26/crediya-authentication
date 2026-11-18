package co.com.crediya.security;

import java.nio.charset.StandardCharsets;

/** Formato compartido de los JWT de CrediYa: emisor, nombres de los claims y la clave HMAC. */
final class JwtClaims {
    static final String ISSUER = "crediya-authentication";
    static final String EMAIL = "correo_electronico";
    static final String ROL = "rol";
    static final String DOCUMENTO = "documento_identidad";
    private static final int MINIMUM_SECRET_BYTES = 32;

    private JwtClaims() {
    }

    /**
     * @throws IllegalStateException when the secret is too short for HS256
     */
    static byte[] secretBytes(String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MINIMUM_SECRET_BYTES) {
            throw new IllegalStateException(
                    "jwt.secret debe tener al menos " + MINIMUM_SECRET_BYTES + " caracteres.");
        }
        return bytes;
    }
}
