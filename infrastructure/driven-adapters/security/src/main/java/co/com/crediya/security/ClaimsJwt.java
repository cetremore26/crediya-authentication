package co.com.crediya.security;

import com.nimbusds.jose.JWSAlgorithm;

/** Formato compartido de los JWT de CrediYa: algoritmo, emisor y nombres de los claims. */
final class ClaimsJwt {
    static final JWSAlgorithm ALGORITMO = JWSAlgorithm.RS256;
    static final String EMISOR = "crediya-authentication";
    static final String CORREO = "correo_electronico";
    static final String ROL = "rol";
    static final String DOCUMENTO = "documento_identidad";

    private ClaimsJwt() {
    }
}
