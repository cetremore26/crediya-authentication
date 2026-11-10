package co.com.crediya.security;

import co.com.crediya.model.auth.AuthenticatedUser;
import co.com.crediya.model.auth.exception.InvalidTokenException;
import co.com.crediya.model.auth.gateways.TokenVerifier;
import co.com.crediya.model.user.Rol;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Verifica los JWT recibidos en las peticiones y extrae la identidad que contienen. */
@Component
public class JwtTokenVerifier implements TokenVerifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenVerifier.class);

    private final byte[] secret;

    public JwtTokenVerifier(@Value("${jwt.secret}") String secret) {
        this.secret = JwtClaims.secretBytes(secret);
    }

    @Override
    public AuthenticatedUser verify(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!jwt.verify(new MACVerifier(secret))) {
                throw new InvalidTokenException("Firma del token inválida.");
            }
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            Date expiration = claims.getExpirationTime();
            if (!JwtClaims.ISSUER.equals(claims.getIssuer())
                    || expiration == null
                    || expiration.before(new Date())) {
                throw new InvalidTokenException("Token expirado o de emisor desconocido.");
            }
            return new AuthenticatedUser(
                    UUID.fromString(claims.getSubject()),
                    claims.getStringClaim(JwtClaims.EMAIL),
                    Rol.valueOf(claims.getStringClaim(JwtClaims.ROL)),
                    claims.getStringClaim(JwtClaims.DOCUMENTO));
        } catch (ParseException | JOSEException | IllegalArgumentException | NullPointerException exception) {
            LOGGER.warn("Token rechazado: {}", exception.getClass().getSimpleName());
            throw new InvalidTokenException("Token inválido.", exception);
        }
    }
}
