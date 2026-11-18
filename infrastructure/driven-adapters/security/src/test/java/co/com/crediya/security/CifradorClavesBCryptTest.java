package co.com.crediya.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CifradorClavesBCryptTest {
    private final CifradorClavesBCrypt hasher = new CifradorClavesBCrypt();

    @Test
    void hashesAndMatchesPassword() {
        String hash = hasher.cifrar("Clave1234");

        assertNotEquals("Clave1234", hash);
        assertTrue(hasher.coincide("Clave1234", hash));
        assertFalse(hasher.coincide("otra", hash));
    }
}
