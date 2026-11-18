package co.com.crediya.security;

import co.com.crediya.model.autenticacion.gateways.ProveedorClavesPublicas;
import com.nimbusds.jose.jwk.JWKSet;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Publica la clave pública de firma como JWKS; nunca incluye la parte privada. */
@Component
public class ProveedorClavesPublicasJwks implements ProveedorClavesPublicas {
    private final ClaveFirmaJwt claveFirma;

    public ProveedorClavesPublicasJwks(ClaveFirmaJwt claveFirma) {
        this.claveFirma = claveFirma;
    }

    @Override
    public Map<String, Object> clavesPublicas() {
        return new JWKSet(claveFirma.clavePublica()).toJSONObject(true);
    }
}
