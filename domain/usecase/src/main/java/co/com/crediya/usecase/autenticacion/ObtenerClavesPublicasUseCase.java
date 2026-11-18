package co.com.crediya.usecase.autenticacion;

import co.com.crediya.model.autenticacion.gateways.ProveedorClavesPublicas;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/** Entrega las claves públicas para que otros servicios verifiquen los tokens de CrediYa. */
@RequiredArgsConstructor
public class ObtenerClavesPublicasUseCase {
    private final ProveedorClavesPublicas proveedorClavesPublicas;

    public Map<String, Object> execute() {
        return proveedorClavesPublicas.clavesPublicas();
    }
}
