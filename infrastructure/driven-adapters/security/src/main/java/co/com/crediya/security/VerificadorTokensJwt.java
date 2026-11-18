package co.com.crediya.security;

import co.com.crediya.model.autenticacion.UsuarioAutenticado;
import co.com.crediya.model.autenticacion.exception.TokenInvalidoException;
import co.com.crediya.model.autenticacion.gateways.VerificadorTokens;
import co.com.crediya.model.usuario.Rol;
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
public class VerificadorTokensJwt implements VerificadorTokens {
    private static final Logger LOGGER = LoggerFactory.getLogger(VerificadorTokensJwt.class);
    private static final Set<String> CLAIMS_OBLIGATORIOS = Set.of(
            JWTClaimNames.SUBJECT, JWTClaimNames.EXPIRATION_TIME, JWTClaimNames.ISSUED_AT,
            ClaimsJwt.CORREO, ClaimsJwt.ROL);

    private final ConfigurableJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();

    public VerificadorTokensJwt(ClaveFirmaJwt claveFirma) {
        processor.setJWSKeySelector(new JWSVerificationKeySelector<>(
                ClaimsJwt.ALGORITMO, new ImmutableJWKSet<>(new JWKSet(claveFirma.clavePublica()))));
        processor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(
                new JWTClaimsSet.Builder().issuer(ClaimsJwt.EMISOR).build(), CLAIMS_OBLIGATORIOS));
    }

    @Override
    public UsuarioAutenticado verificar(String token) {
        try {
            JWTClaimsSet claims = processor.process(token, null);
            return new UsuarioAutenticado(
                    UUID.fromString(claims.getSubject()),
                    claims.getStringClaim(ClaimsJwt.CORREO),
                    Rol.valueOf(claims.getStringClaim(ClaimsJwt.ROL)),
                    claims.getStringClaim(ClaimsJwt.DOCUMENTO));
        } catch (ParseException | BadJOSEException | JOSEException | IllegalArgumentException exception) {
            LOGGER.warn("Token rechazado: {}", exception.getClass().getSimpleName());
            throw new TokenInvalidoException("Token inválido.", exception);
        }
    }
}
