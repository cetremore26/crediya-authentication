package co.com.crediya.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BCryptPasswordHasherTest {
    private final BCryptPasswordHasher hasher = new BCryptPasswordHasher();

    @Test
    void hashesAndMatchesPassword() {
        String hash = hasher.hash("Clave1234");

        assertNotEquals("Clave1234", hash);
        assertTrue(hasher.matches("Clave1234", hash));
        assertFalse(hasher.matches("otra", hash));
    }
}
