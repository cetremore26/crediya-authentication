package co.com.crediya.security;

import com.nimbusds.jose.JWSAlgorithm;

/** Formato compartido de los JWT de CrediYa: algoritmo, emisor y nombres de los claims. */
final class JwtClaims {
    static final JWSAlgorithm ALGORITHM = JWSAlgorithm.RS256;
    static final String ISSUER = "crediya-authentication";
    static final String EMAIL = "correo_electronico";
    static final String ROL = "rol";
    static final String DOCUMENTO = "documento_identidad";

    private JwtClaims() {
    }
}
