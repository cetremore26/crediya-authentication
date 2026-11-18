package co.com.crediya.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class JwtSigningKeyTest {

    @Test
    void loadsAPkcs8PemAndDerivesThePublicKey() throws Exception {
        JwtSigningKey key = new JwtSigningKey(TestKeys.privateKeyPem(), false);

        assertNotNull(key.rsaKey().toPrivateKey());
        assertEquals(key.rsaKey().getModulus(), key.publicKey().getModulus());
        assertNotNull(key.publicKey().getKeyID());
    }

    @Test
    void acceptsPemWithEscapedNewlinesAsInEnvironmentVariables() {
        String escaped = TestKeys.privateKeyPem().replace("\n", "\\n");

        JwtSigningKey key = new JwtSigningKey(escaped, false);

        assertEquals(
                new JwtSigningKey(TestKeys.privateKeyPem(), false).publicKey().getKeyID(),
                key.publicKey().getKeyID());
    }

    @Test
    void requiresAKeyUnlessEphemeralKeysAreAllowed() {
        assertThrows(IllegalStateException.class, () -> new JwtSigningKey("", false));
        assertThrows(IllegalStateException.class, () -> new JwtSigningKey(null, false));
    }

    @Test
    void generatesADifferentEphemeralKeyEachTime() {
        assertNotEquals(
                new JwtSigningKey("", true).publicKey().getKeyID(),
                new JwtSigningKey("", true).publicKey().getKeyID());
    }

    @Test
    void rejectsInvalidOrWeakKeys() {
        assertThrows(IllegalStateException.class, () -> new JwtSigningKey("no-es-un-pem", false));
        assertThrows(IllegalStateException.class,
                () -> new JwtSigningKey(TestKeys.generatePem(1024), false));
    }
}
