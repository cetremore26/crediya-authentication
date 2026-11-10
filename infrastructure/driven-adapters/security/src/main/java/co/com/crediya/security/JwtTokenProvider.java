package co.com.crediya.security;

import co.com.crediya.model.auth.AccessToken;
import co.com.crediya.model.auth.gateways.TokenProvider;
import co.com.crediya.model.user.User;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.time.Instant;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Emite los JWT firmados con HS256 al iniciar sesión. */
@Component
public class JwtTokenProvider implements TokenProvider {
    private final byte[] secret;
    private final long expirationSeconds;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-seconds}") long expirationSeconds) {
        this.secret = JwtClaims.secretBytes(secret);
        this.expirationSeconds = expirationSeconds;
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
                .expirationTime(Date.from(now.plusSeconds(expirationSeconds)))
                .build();
        try {
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(secret));
            return new AccessToken(jwt.serialize(), expirationSeconds);
        } catch (JOSEException exception) {
            throw new IllegalStateException("No fue posible firmar el token.", exception);
        }
    }
}
