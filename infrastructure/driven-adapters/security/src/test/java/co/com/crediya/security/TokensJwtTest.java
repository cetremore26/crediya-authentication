package co.com.crediya.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.com.crediya.model.autenticacion.TokenAcceso;
import co.com.crediya.model.autenticacion.UsuarioAutenticado;
import co.com.crediya.model.autenticacion.exception.TokenInvalidoException;
import co.com.crediya.model.usuario.Rol;
import co.com.crediya.model.usuario.Usuario;
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

/** Tokens emitidos por {@link EmisorTokensJwt} y verificados por {@link VerificadorTokensJwt}. */
class TokensJwtTest {
    private final ClaveFirmaJwt key = new ClaveFirmaJwt(ClavesDePrueba.clavePrivadaPem(), false);
    private final EmisorTokensJwt provider = new EmisorTokensJwt(key, 3600);
    private final VerificadorTokensJwt verifier = new VerificadorTokensJwt(key);

    @Test
    void generatedTokenIsRs256AndCarriesIdentityAndRole() throws Exception {
        UUID id = UUID.randomUUID();
        TokenAcceso token = provider.emitir(usuario(id, Rol.CLIENTE, "1020304050"));

        UsuarioAutenticado autenticado = verifier.verificar(token.token());

        assertEquals(JWSAlgorithm.RS256, SignedJWT.parse(token.token()).getHeader().getAlgorithm());
        assertEquals(3600, token.expiraEnSegundos());
        assertEquals(id, autenticado.idUsuario());
        assertEquals(Rol.CLIENTE, autenticado.rol());
        assertEquals("1020304050", autenticado.documentoIdentidad());
    }

    @Test
    void staffTokensHaveNoDocument() {
        TokenAcceso token = provider.emitir(usuario(UUID.randomUUID(), Rol.ADMIN, null));

        assertNull(verifier.verificar(token.token()).documentoIdentidad());
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() {
        EmisorTokensJwt other = new EmisorTokensJwt(new ClaveFirmaJwt("", true), 3600);
        String token = other.emitir(usuario(UUID.randomUUID(), Rol.ADMIN, null)).token();

        assertThrows(TokenInvalidoException.class, () -> verifier.verificar(token));
    }

    @Test
    void rejectsHmacTokensEvenWithTheRightClaims() throws Exception {
        SignedJWT hmac = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), validClaims().build());
        hmac.sign(new MACSigner("una-clave-hmac-de-al-menos-32-caracteres!!"));

        assertThrows(TokenInvalidoException.class, () -> verifier.verificar(hmac.serialize()));
    }

    @Test
    void rejectsExpiredToken() {
        EmisorTokensJwt expired = new EmisorTokensJwt(key, -120);
        String token = expired.emitir(usuario(UUID.randomUUID(), Rol.ADMIN, null)).token();

        assertThrows(TokenInvalidoException.class, () -> verifier.verificar(token));
    }

    @Test
    void rejectsTokensWithMissingOrUnknownClaims() {
        assertThrows(TokenInvalidoException.class,
                () -> verifier.verificar(sign(validClaims().claim(ClaimsJwt.ROL, null))));
        assertThrows(TokenInvalidoException.class,
                () -> verifier.verificar(sign(validClaims().claim(ClaimsJwt.ROL, "SUPERUSUARIO"))));
        assertThrows(TokenInvalidoException.class,
                () -> verifier.verificar(sign(validClaims().subject("no-es-uuid"))));
        assertThrows(TokenInvalidoException.class,
                () -> verifier.verificar(sign(validClaims().issuer("otro-emisor"))));
    }

    @Test
    void rejectsTokensNotYetValid() {
        Date future = Date.from(Instant.now().plusSeconds(600));

        assertThrows(TokenInvalidoException.class,
                () -> verifier.verificar(sign(validClaims().notBeforeTime(future))));
    }

    @Test
    void rejectsGarbage() {
        assertThrows(TokenInvalidoException.class, () -> verifier.verificar("no-es-un-jwt"));
    }

    @Test
    void publishesOnlyThePublicKey() {
        Map<String, Object> jwks = new ProveedorClavesPublicasJwks(key).clavesPublicas();

        @SuppressWarnings("unchecked")
        Map<String, Object> jwk = ((java.util.List<Map<String, Object>>) jwks.get("keys")).get(0);
        assertEquals("RSA", jwk.get("kty"));
        assertFalse(jwk.containsKey("d"), "la clave privada no se publica");
    }

    private String sign(JWTClaimsSet.Builder claims) throws Exception {
        SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.claveCompleta().getKeyID()).build(),
                claims.build());
        jwt.sign(new com.nimbusds.jose.crypto.RSASSASigner(key.claveCompleta()));
        return jwt.serialize();
    }

    private static JWTClaimsSet.Builder validClaims() {
        Instant now = Instant.now();
        return new JWTClaimsSet.Builder()
                .issuer(ClaimsJwt.EMISOR)
                .subject(UUID.randomUUID().toString())
                .claim(ClaimsJwt.CORREO, "ana@example.com")
                .claim(ClaimsJwt.ROL, "ADMIN")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(300)));
    }

    private Usuario usuario(UUID id, Rol rol, String documento) {
        return Usuario.builder()
                .id(id)
                .correoElectronico("ana@example.com")
                .rol(rol)
                .documentoIdentidad(documento)
                .build();
    }
}
