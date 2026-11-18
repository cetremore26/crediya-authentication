package co.com.crediya.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.com.crediya.model.auth.AccessToken;
import co.com.crediya.model.auth.AuthenticatedUser;
import co.com.crediya.model.auth.exception.InvalidTokenException;
import co.com.crediya.model.user.Rol;
import co.com.crediya.model.user.User;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Tokens emitidos por {@link JwtTokenProvider} y verificados por {@link JwtTokenVerifier}. */
class JwtTokenTest {
    private final JwtSigningKey key = new JwtSigningKey(TestKeys.privateKeyPem(), false);
    private final JwtTokenProvider provider = new JwtTokenProvider(key, 3600);
    private final JwtTokenVerifier verifier = new JwtTokenVerifier(key);

    @Test
    void generatedTokenIsRs256AndCarriesIdentityAndRole() throws Exception {
        UUID id = UUID.randomUUID();
        AccessToken token = provider.generate(user(id, Rol.CLIENTE, "1020304050"));

        AuthenticatedUser authenticated = verifier.verify(token.token());

        assertEquals(JWSAlgorithm.RS256, SignedJWT.parse(token.token()).getHeader().getAlgorithm());
        assertEquals(3600, token.expiresInSeconds());
        assertEquals(id, authenticated.userId());
        assertEquals(Rol.CLIENTE, authenticated.rol());
        assertEquals("1020304050", authenticated.documentoIdentidad());
    }

    @Test
    void staffTokensHaveNoDocument() {
        AccessToken token = provider.generate(user(UUID.randomUUID(), Rol.ADMIN, null));

        assertNull(verifier.verify(token.token()).documentoIdentidad());
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() {
        JwtTokenProvider other = new JwtTokenProvider(new JwtSigningKey("", true), 3600);
        String token = other.generate(user(UUID.randomUUID(), Rol.ADMIN, null)).token();

        assertThrows(InvalidTokenException.class, () -> verifier.verify(token));
    }

    @Test
    void rejectsHmacTokensEvenWithTheRightClaims() throws Exception {
        SignedJWT hmac = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), validClaims().build());
        hmac.sign(new MACSigner("una-clave-hmac-de-al-menos-32-caracteres!!"));

        assertThrows(InvalidTokenException.class, () -> verifier.verify(hmac.serialize()));
    }

    @Test
    void rejectsExpiredToken() {
        JwtTokenProvider expired = new JwtTokenProvider(key, -120);
        String token = expired.generate(user(UUID.randomUUID(), Rol.ADMIN, null)).token();

        assertThrows(InvalidTokenException.class, () -> verifier.verify(token));
    }

    @Test
    void rejectsTokensWithMissingOrUnknownClaims() {
        assertThrows(InvalidTokenException.class,
                () -> verifier.verify(sign(validClaims().claim(JwtClaims.ROL, null))));
        assertThrows(InvalidTokenException.class,
                () -> verifier.verify(sign(validClaims().claim(JwtClaims.ROL, "SUPERUSUARIO"))));
        assertThrows(InvalidTokenException.class,
                () -> verifier.verify(sign(validClaims().subject("no-es-uuid"))));
        assertThrows(InvalidTokenException.class,
                () -> verifier.verify(sign(validClaims().issuer("otro-emisor"))));
    }

    @Test
    void rejectsTokensNotYetValid() {
        Date future = Date.from(Instant.now().plusSeconds(600));

        assertThrows(InvalidTokenException.class,
                () -> verifier.verify(sign(validClaims().notBeforeTime(future))));
    }

    @Test
    void rejectsGarbage() {
        assertThrows(InvalidTokenException.class, () -> verifier.verify("no-es-un-jwt"));
    }

    @Test
    void publishesOnlyThePublicKey() {
        Map<String, Object> jwks = new JwksPublicKeySetProvider(key).publicKeySet();

        @SuppressWarnings("unchecked")
        Map<String, Object> jwk = ((java.util.List<Map<String, Object>>) jwks.get("keys")).get(0);
        assertEquals("RSA", jwk.get("kty"));
        assertFalse(jwk.containsKey("d"), "la clave privada no se publica");
    }

    private String sign(JWTClaimsSet.Builder claims) throws Exception {
        SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.rsaKey().getKeyID()).build(),
                claims.build());
        jwt.sign(new com.nimbusds.jose.crypto.RSASSASigner(key.rsaKey()));
        return jwt.serialize();
    }

    private static JWTClaimsSet.Builder validClaims() {
        Instant now = Instant.now();
        return new JWTClaimsSet.Builder()
                .issuer(JwtClaims.ISSUER)
                .subject(UUID.randomUUID().toString())
                .claim(JwtClaims.EMAIL, "ana@example.com")
                .claim(JwtClaims.ROL, "ADMIN")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(300)));
    }

    private User user(UUID id, Rol rol, String documento) {
        return User.builder()
                .id(id)
                .correoElectronico("ana@example.com")
                .rol(rol)
                .documentoIdentidad(documento)
                .build();
    }
}
