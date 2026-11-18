package co.com.crediya.model.autenticacion.gateways;

import java.util.Map;

/**
 * Publica las claves con las que otros servicios verifican los tokens emitidos aquí.
 * Solo expone claves públicas: la privada nunca sale del servicio de autenticación.
 */
public interface ProveedorClavesPublicas {
    /** Conjunto de claves públicas en formato JWKS (RFC 7517). */
    Map<String, Object> clavesPublicas();
}
