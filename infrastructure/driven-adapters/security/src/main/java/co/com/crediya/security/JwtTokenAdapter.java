package co.com.crediya.security;

import co.com.crediya.model.auth.AccessToken;
import co.com.crediya.model.auth.AuthenticatedUser;
import co.com.crediya.model.auth.exception.InvalidTokenException;
import co.com.crediya.model.auth.gateways.TokenProvider;
import co.com.crediya.model.auth.gateways.TokenVerifier;
import co.com.crediya.model.user.Rol;
import co.com.crediya.model.user.User;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenAdapter implements TokenProvider, TokenVerifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenAdapter.class);
    private static final String ISSUER = "crediya-authentication";
    private static final String CLAIM_EMAIL = "correo_electronico";
    private static final String CLAIM_ROL = "rol";
    private static final String CLAIM_DOCUMENTO = "documento_identidad";
    private static final int MINIMUM_SECRET_BYTES = 32;

    private final byte[] secret;
    private final long expirationSeconds;

    public JwtTokenAdapter(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-seconds}") long expirationSeconds) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        if (this.secret.length < MINIMUM_SECRET_BYTES) {
            throw new IllegalStateException(
                    "jwt.secret debe tener al menos " + MINIMUM_SECRET_BYTES + " caracteres.");
        }
        this.expirationSeconds = expirationSeconds;
    }

    @Override
    public AccessToken generate(User user) {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(ISSUER)
                .subject(user.getId().toString())
                .claim(CLAIM_EMAIL, user.getCorreoElectronico())
                .claim(CLAIM_ROL, user.getRol().name())
                .claim(CLAIM_DOCUMENTO, user.getDocumentoIdentidad())
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

    @Override
    public AuthenticatedUser verify(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!jwt.verify(new MACVerifier(secret))) {
                throw new InvalidTokenException("Firma del token inválida.");
            }
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            Date expiration = claims.getExpirationTime();
            if (!ISSUER.equals(claims.getIssuer())
                    || expiration == null
                    || expiration.before(new Date())) {
                throw new InvalidTokenException("Token expirado o de emisor desconocido.");
            }
            return new AuthenticatedUser(
                    UUID.fromString(claims.getSubject()),
                    claims.getStringClaim(CLAIM_EMAIL),
                    Rol.valueOf(claims.getStringClaim(CLAIM_ROL)),
                    claims.getStringClaim(CLAIM_DOCUMENTO));
        } catch (ParseException | JOSEException | IllegalArgumentException | NullPointerException exception) {
            LOGGER.warn("Token rechazado: {}", exception.getClass().getSimpleName());
            throw new InvalidTokenException("Token inválido.", exception);
        }
    }
}
