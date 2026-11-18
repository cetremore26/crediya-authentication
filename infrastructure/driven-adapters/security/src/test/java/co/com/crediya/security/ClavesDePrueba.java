package co.com.crediya.security;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/** Claves RSA generadas al vuelo para las pruebas, en el mismo PEM que recibe la aplicación. */
final class ClavesDePrueba {
    private static final String PRIVATE_KEY_PEM = generarPem(2048);

    private ClavesDePrueba() {
    }

    static String clavePrivadaPem() {
        return PRIVATE_KEY_PEM;
    }

    static String generarPem(int bits) {
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
