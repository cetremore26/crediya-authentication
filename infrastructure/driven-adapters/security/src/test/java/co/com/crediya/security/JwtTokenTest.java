package co.com.crediya.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.com.crediya.model.auth.AccessToken;
import co.com.crediya.model.auth.AuthenticatedUser;
import co.com.crediya.model.auth.exception.InvalidTokenException;
import co.com.crediya.model.user.Rol;
import co.com.crediya.model.user.User;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Tokens emitidos por {@link JwtTokenProvider} y verificados por {@link JwtTokenVerifier}. */
class JwtTokenTest {
    private static final String SECRET = "una-clave-de-pruebas-de-al-menos-32-caracteres";

    private final JwtTokenProvider provider = new JwtTokenProvider(SECRET, 3600);
    private final JwtTokenVerifier verifier = new JwtTokenVerifier(SECRET);

    @Test
    void generatedTokenCarriesIdentityAndRole() {
        UUID id = UUID.randomUUID();
        AccessToken token = provider.generate(user(id, Rol.CLIENTE, "1020304050"));

        AuthenticatedUser authenticated = verifier.verify(token.token());

        assertEquals(3600, token.expiresInSeconds());
        assertEquals(id, authenticated.userId());
        assertEquals(Rol.CLIENTE, authenticated.rol());
        assertEquals("1020304050", authenticated.documentoIdentidad());
    }

    @Test
    void staffTokensHaveNoDocument() {
        AccessToken token = provider.generate(user(UUID.randomUUID(), Rol.ADMIN, null));

        assertNull(verifier.verify(token.token()).documentoIdentidad());
    }

    @Test
    void rejectsTokenSignedWithAnotherSecret() {
        JwtTokenProvider other =
                new JwtTokenProvider("otra-clave-de-pruebas-de-al-menos-32-caracteres", 3600);
        String token = other.generate(user(UUID.randomUUID(), Rol.ADMIN, null)).token();

        assertThrows(InvalidTokenException.class, () -> verifier.verify(token));
    }

    @Test
    void rejectsExpiredToken() {
        JwtTokenProvider expired = new JwtTokenProvider(SECRET, -10);
        String token = expired.generate(user(UUID.randomUUID(), Rol.ADMIN, null)).token();

        assertThrows(InvalidTokenException.class, () -> verifier.verify(token));
    }

    @Test
    void rejectsGarbage() {
        assertThrows(InvalidTokenException.class, () -> verifier.verify("no-es-un-jwt"));
    }

    @Test
    void rejectsShortSecret() {
        assertThrows(IllegalStateException.class, () -> new JwtTokenProvider("corta", 3600));
        assertThrows(IllegalStateException.class, () -> new JwtTokenVerifier("corta"));
    }

    private User user(UUID id, Rol rol, String documento) {
        return User.builder()
                .id(id)
                .correoElectronico("ana@example.com")
                .rol(rol)
                .documentoIdentidad(documento)
                .build();
    }
}
