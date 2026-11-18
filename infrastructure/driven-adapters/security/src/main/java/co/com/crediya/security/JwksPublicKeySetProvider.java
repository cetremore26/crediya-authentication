package co.com.crediya.security;

import co.com.crediya.model.auth.gateways.PublicKeySetProvider;
import com.nimbusds.jose.jwk.JWKSet;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Publica la clave pública de firma como JWKS; nunca incluye la parte privada. */
@Component
public class JwksPublicKeySetProvider implements PublicKeySetProvider {
    private final JwtSigningKey signingKey;

    public JwksPublicKeySetProvider(JwtSigningKey signingKey) {
        this.signingKey = signingKey;
    }

    @Override
    public Map<String, Object> publicKeySet() {
        return new JWKSet(signingKey.publicKey()).toJSONObject(true);
    }
}
