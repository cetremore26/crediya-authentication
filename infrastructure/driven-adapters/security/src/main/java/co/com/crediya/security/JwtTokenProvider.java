package co.com.crediya.security;

import co.com.crediya.model.auth.AccessToken;
import co.com.crediya.model.auth.gateways.TokenProvider;
import co.com.crediya.model.user.User;
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
public class JwtTokenProvider implements TokenProvider {
    private final JwtSigningKey signingKey;
    private final RSASSASigner signer;
    private final long expirationSeconds;

    public JwtTokenProvider(
            JwtSigningKey signingKey,
            @Value("${jwt.expiration-seconds}") long expirationSeconds) {
        this.signingKey = signingKey;
        this.expirationSeconds = expirationSeconds;
        try {
            this.signer = new RSASSASigner(signingKey.rsaKey());
        } catch (JOSEException exception) {
            throw new IllegalStateException("La clave JWT no sirve para firmar.", exception);
        }
    }

    @Override
    public AccessToken generate(User user) {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(JwtClaims.ISSUER)
                .subject(user.getId().toString())
                .claim(JwtClaims.EMAIL, user.getCorreoElectronico())
                .claim(JwtClaims.ROL, user.getRol().name())
                .claim(JwtClaims.DOCUMENTO, user.getDocumentoIdentidad())
                .issueTime(Date.from(now))
                .notBeforeTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(expirationSeconds)))
                .build();
        JWSHeader header = new JWSHeader.Builder(JwtClaims.ALGORITHM)
                .keyID(signingKey.rsaKey().getKeyID())
                .build();
        try {
            SignedJWT jwt = new SignedJWT(header, claims);
            jwt.sign(signer);
            return new AccessToken(jwt.serialize(), expirationSeconds);
        } catch (JOSEException exception) {
            throw new IllegalStateException("No fue posible firmar el token.", exception);
        }
    }
}
