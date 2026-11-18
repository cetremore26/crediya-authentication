package co.com.crediya.usecase.auth;

import co.com.crediya.model.auth.gateways.PublicKeySetProvider;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/** Entrega las claves públicas para que otros servicios verifiquen los tokens de CrediYa. */
@RequiredArgsConstructor
public class GetPublicKeySetUseCase {
    private final PublicKeySetProvider publicKeySetProvider;

    public Map<String, Object> execute() {
        return publicKeySetProvider.publicKeySet();
    }
}
