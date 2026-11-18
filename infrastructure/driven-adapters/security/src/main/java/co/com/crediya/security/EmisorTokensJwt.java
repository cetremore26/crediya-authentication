package co.com.crediya.security;

import co.com.crediya.model.autenticacion.TokenAcceso;
import co.com.crediya.model.autenticacion.gateways.EmisorTokens;
import co.com.crediya.model.usuario.Usuario;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.time.Instant;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Emite los JWT firmados con RS256 al iniciar sesión. */
@Component
public class EmisorTokensJwt implements EmisorTokens {
    private final ClaveFirmaJwt claveFirma;
    private final RSASSASigner signer;
    private final long expirationSeconds;

    public EmisorTokensJwt(
            ClaveFirmaJwt claveFirma,
            @Value("${jwt.expiration-seconds}") long expirationSeconds) {
        this.claveFirma = claveFirma;
        this.expirationSeconds = expirationSeconds;
        try {
            this.signer = new RSASSASigner(claveFirma.claveCompleta());
        } catch (JOSEException exception) {
            throw new IllegalStateException("La clave JWT no sirve para firmar.", exception);
        }
    }

    @Override
    public TokenAcceso emitir(Usuario usuario) {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(ClaimsJwt.EMISOR)
                .subject(usuario.getId().toString())
                .claim(ClaimsJwt.CORREO, usuario.getCorreoElectronico())
                .claim(ClaimsJwt.ROL, usuario.getRol().name())
                .claim(ClaimsJwt.DOCUMENTO, usuario.getDocumentoIdentidad())
                .issueTime(Date.from(now))
                .notBeforeTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(expirationSeconds)))
                .build();
        JWSHeader header = new JWSHeader.Builder(ClaimsJwt.ALGORITMO)
                .keyID(claveFirma.claveCompleta().getKeyID())
                .build();
        try {
            SignedJWT jwt = new SignedJWT(header, claims);
            jwt.sign(signer);
            return new TokenAcceso(jwt.serialize(), expirationSeconds);
        } catch (JOSEException exception) {
            throw new IllegalStateException("No fue posible firmar el token.", exception);
        }
    }
}
