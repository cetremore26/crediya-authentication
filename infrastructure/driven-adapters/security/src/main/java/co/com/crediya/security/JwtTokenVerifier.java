package co.com.crediya.security;

import co.com.crediya.model.auth.AuthenticatedUser;
import co.com.crediya.model.auth.exception.InvalidTokenException;
import co.com.crediya.model.auth.gateways.TokenVerifier;
import co.com.crediya.model.user.Rol;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import java.text.ParseException;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Verifica los JWT recibidos en las peticiones y extrae la identidad que contienen.
 * Exige RS256 con la clave pública de este servicio, el emisor de CrediYa, vigencia
 * ({@code exp}/{@code nbf}) y que estén todos los claims de identidad.
 */
@Component
public class JwtTokenVerifier implements TokenVerifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenVerifier.class);
    private static final Set<String> REQUIRED_CLAIMS = Set.of(
            JWTClaimNames.SUBJECT, JWTClaimNames.EXPIRATION_TIME, JWTClaimNames.ISSUED_AT,
            JwtClaims.EMAIL, JwtClaims.ROL);

    private final ConfigurableJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();

    public JwtTokenVerifier(JwtSigningKey signingKey) {
        processor.setJWSKeySelector(new JWSVerificationKeySelector<>(
                JwtClaims.ALGORITHM, new ImmutableJWKSet<>(new JWKSet(signingKey.publicKey()))));
        processor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(
                new JWTClaimsSet.Builder().issuer(JwtClaims.ISSUER).build(), REQUIRED_CLAIMS));
    }

    @Override
    public AuthenticatedUser verify(String token) {
        try {
            JWTClaimsSet claims = processor.process(token, null);
            return new AuthenticatedUser(
                    UUID.fromString(claims.getSubject()),
                    claims.getStringClaim(JwtClaims.EMAIL),
                    Rol.valueOf(claims.getStringClaim(JwtClaims.ROL)),
                    claims.getStringClaim(JwtClaims.DOCUMENTO));
        } catch (ParseException | BadJOSEException | JOSEException | IllegalArgumentException exception) {
            LOGGER.warn("Token rechazado: {}", exception.getClass().getSimpleName());
            throw new InvalidTokenException("Token inválido.", exception);
        }
    }
}
