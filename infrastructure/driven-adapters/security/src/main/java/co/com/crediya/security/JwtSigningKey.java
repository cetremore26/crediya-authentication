package co.com.crediya.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Par de claves RSA con el que se firman (privada) y verifican (pública) los tokens.
 *
 * <p>La clave privada llega en {@code jwt.private-key} como PEM PKCS#8
 * ({@code -----BEGIN PRIVATE KEY-----}); la pública se deriva de ella. Solo para desarrollo,
 * con {@code jwt.generate-ephemeral-key=true} se genera una clave temporal al arrancar
 * (los tokens dejan de valer al reiniciar).
 */
@Component
public class JwtSigningKey {
    static final int MINIMUM_KEY_BITS = 2048;
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtSigningKey.class);

    private final RSAKey rsaKey;

    public JwtSigningKey(
            @Value("${jwt.private-key:}") String privateKeyPem,
            @Value("${jwt.generate-ephemeral-key:false}") boolean generateEphemeralKey) {
        this.rsaKey = privateKeyPem == null || privateKeyPem.isBlank()
                ? ephemeralKey(generateEphemeralKey)
                : fromPem(privateKeyPem);
    }

    /** Clave completa (privada y pública), solo para firmar. */
    RSAKey rsaKey() {
        return rsaKey;
    }

    /** Solo la parte pública, que se puede publicar y usar para verificar. */
    RSAKey publicKey() {
        return rsaKey.toPublicJWK();
    }

    private static RSAKey ephemeralKey(boolean allowed) {
        if (!allowed) {
            throw new IllegalStateException(
                    "jwt.private-key (JWT_PRIVATE_KEY) es obligatorio: una clave RSA privada en PEM PKCS#8.");
        }
        LOGGER.warn("Usando una clave JWT temporal: solo apta para desarrollo local.");
        try {
            return new RSAKeyGenerator(MINIMUM_KEY_BITS)
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JwtClaims.ALGORITHM)
                    .keyIDFromThumbprint(true)
                    .generate();
        } catch (JOSEException exception) {
            throw new IllegalStateException("No fue posible generar la clave JWT temporal.", exception);
        }
    }

    static RSAKey fromPem(String pem) {
        try {
            String base64 = pem.replace("\\n", "\n")
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPrivateCrtKey privateKey = (RSAPrivateCrtKey) keyFactory.generatePrivate(
                    new PKCS8EncodedKeySpec(Base64.getDecoder().decode(base64)));
            if (privateKey.getModulus().bitLength() < MINIMUM_KEY_BITS) {
                throw new IllegalStateException(
                        "jwt.private-key debe ser una clave RSA de al menos " + MINIMUM_KEY_BITS + " bits.");
            }
            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(
                    new RSAPublicKeySpec(privateKey.getModulus(), privateKey.getPublicExponent()));
            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JwtClaims.ALGORITHM)
                    .keyIDFromThumbprint()
                    .build();
        } catch (GeneralSecurityException | IllegalArgumentException | ClassCastException
                 | JOSEException exception) {
            throw new IllegalStateException(
                    "jwt.private-key no es una clave RSA privada válida en PEM PKCS#8.", exception);
        }
    }
}
