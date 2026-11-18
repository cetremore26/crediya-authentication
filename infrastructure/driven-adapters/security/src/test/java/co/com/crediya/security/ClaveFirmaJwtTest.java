package co.com.crediya.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ClaveFirmaJwtTest {

    @Test
    void loadsAPkcs8PemAndDerivesThePublicKey() throws Exception {
        ClaveFirmaJwt key = new ClaveFirmaJwt(ClavesDePrueba.clavePrivadaPem(), false);

        assertNotNull(key.claveCompleta().toPrivateKey());
        assertEquals(key.claveCompleta().getModulus(), key.clavePublica().getModulus());
        assertNotNull(key.clavePublica().getKeyID());
    }

    @Test
    void acceptsPemWithEscapedNewlinesAsInEnvironmentVariables() {
        String escaped = ClavesDePrueba.clavePrivadaPem().replace("\n", "\\n");

        ClaveFirmaJwt key = new ClaveFirmaJwt(escaped, false);

        assertEquals(
                new ClaveFirmaJwt(ClavesDePrueba.clavePrivadaPem(), false).clavePublica().getKeyID(),
                key.clavePublica().getKeyID());
    }

    @Test
    void requiresAKeyUnlessEphemeralKeysAreAllowed() {
        assertThrows(IllegalStateException.class, () -> new ClaveFirmaJwt("", false));
        assertThrows(IllegalStateException.class, () -> new ClaveFirmaJwt(null, false));
    }

    @Test
    void generatesADifferentEphemeralKeyEachTime() {
        assertNotEquals(
                new ClaveFirmaJwt("", true).clavePublica().getKeyID(),
                new ClaveFirmaJwt("", true).clavePublica().getKeyID());
    }

    @Test
    void rejectsInvalidOrWeakKeys() {
        assertThrows(IllegalStateException.class, () -> new ClaveFirmaJwt("no-es-un-pem", false));
        assertThrows(IllegalStateException.class,
                () -> new ClaveFirmaJwt(ClavesDePrueba.generarPem(1024), false));
    }
}
