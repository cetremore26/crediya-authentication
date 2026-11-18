package co.com.crediya.security;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/** Claves RSA generadas al vuelo para las pruebas, en el mismo PEM que recibe la aplicación. */
final class TestKeys {
    private static final String PRIVATE_KEY_PEM = generatePem(2048);

    private TestKeys() {
    }

    static String privateKeyPem() {
        return PRIVATE_KEY_PEM;
    }

    static String generatePem(int bits) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(bits);
            byte[] pkcs8 = generator.generateKeyPair().getPrivate().getEncoded();
            return "-----BEGIN PRIVATE KEY-----\n"
                    + Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(pkcs8)
                    + "\n-----END PRIVATE KEY-----\n";
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
