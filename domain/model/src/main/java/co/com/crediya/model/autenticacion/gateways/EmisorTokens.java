package co.com.crediya.model.autenticacion.gateways;

import co.com.crediya.model.autenticacion.TokenAcceso;
import co.com.crediya.model.usuario.Usuario;

public interface EmisorTokens {
    TokenAcceso emitir(Usuario usuario);
}
