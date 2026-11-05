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

class JwtTokenAdapterTest {
    private static final String SECRET = "una-clave-de-pruebas-de-al-menos-32-caracteres";

    private final JwtTokenAdapter adapter = new JwtTokenAdapter(SECRET, 3600);

    @Test
    void generatedTokenCarriesIdentityAndRole() {
        UUID id = UUID.randomUUID();
        AccessToken token = adapter.generate(user(id, Rol.CLIENTE, "1020304050"));

        AuthenticatedUser authenticated = adapter.verify(token.token());

        assertEquals(3600, token.expiresInSeconds());
        assertEquals(id, authenticated.userId());
        assertEquals(Rol.CLIENTE, authenticated.rol());
        assertEquals("1020304050", authenticated.documentoIdentidad());
    }

    @Test
    void staffTokensHaveNoDocument() {
        AccessToken token = adapter.generate(user(UUID.randomUUID(), Rol.ADMIN, null));

        assertNull(adapter.verify(token.token()).documentoIdentidad());
    }

    @Test
    void rejectsTokenSignedWithAnotherSecret() {
        JwtTokenAdapter other =
                new JwtTokenAdapter("otra-clave-de-pruebas-de-al-menos-32-caracteres", 3600);
        String token = other.generate(user(UUID.randomUUID(), Rol.ADMIN, null)).token();

        assertThrows(InvalidTokenException.class, () -> adapter.verify(token));
    }

    @Test
    void rejectsExpiredToken() {
        JwtTokenAdapter expired = new JwtTokenAdapter(SECRET, -10);
        String token = expired.generate(user(UUID.randomUUID(), Rol.ADMIN, null)).token();

        assertThrows(InvalidTokenException.class, () -> adapter.verify(token));
    }

    @Test
    void rejectsGarbage() {
        assertThrows(InvalidTokenException.class, () -> adapter.verify("no-es-un-jwt"));
    }

    @Test
    void rejectsShortSecret() {
        assertThrows(IllegalStateException.class, () -> new JwtTokenAdapter("corta", 3600));
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
