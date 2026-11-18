package co.com.crediya.usecase.usuario;

import co.com.crediya.model.autenticacion.gateways.CifradorClaves;
import co.com.crediya.model.usuario.Rol;
import co.com.crediya.model.usuario.Usuario;
import co.com.crediya.model.usuario.exception.DocumentoYaRegistradoException;
import co.com.crediya.model.usuario.exception.RolNoPermitidoException;
import co.com.crediya.model.usuario.exception.UsuarioYaRegistradoException;
import co.com.crediya.model.usuario.exception.UsuarioInvalidoException;
import co.com.crediya.model.usuario.gateways.UsuarioRepository;
import lombok.RequiredArgsConstructor;

/**
 * Registra un usuario nuevo. Solo orquesta el flujo: las reglas de qué es un usuario
 * válido viven en {@link Usuario} y las de quién puede asignar cada rol en {@link Rol}.
 */
@RequiredArgsConstructor
public class RegistrarUsuarioUseCase {
    private final UsuarioRepository usuarioRepository;
    private final CifradorClaves cifradorClaves;

    /**
     * @param usuario datos del usuario a registrar
     * @param rolSolicitante rol de quien hace el registro
     * @throws RolNoPermitidoException cuando ese rol no puede asignar el rol del usuario nuevo
     */
    public Usuario execute(Usuario usuario, Rol rolSolicitante) {
        if (usuario == null) {
            throw new UsuarioInvalidoException("El usuario es obligatorio.");
        }

        Usuario candidato = usuario.normalizado();
        validarPuedeAsignarRol(rolSolicitante, candidato.getRol());
        candidato.validar();
        validarNoRegistrado(candidato);

        return usuarioRepository.guardar(candidato.toBuilder()
                .clave(cifradorClaves.cifrar(candidato.getClave()))
                .build());
    }

    private static void validarPuedeAsignarRol(Rol rolSolicitante, Rol rolNuevo) {
        if (rolSolicitante == null || !rolSolicitante.puedeAsignar(rolNuevo)) {
            throw new RolNoPermitidoException(rolNuevo);
        }
    }

    private void validarNoRegistrado(Usuario usuario) {
        if (usuarioRepository.existePorCorreoElectronico(usuario.getCorreoElectronico())) {
            throw new UsuarioYaRegistradoException();
        }
        String documento = usuario.getDocumentoIdentidad();
        if (documento != null && usuarioRepository.existePorDocumentoIdentidad(documento)) {
            throw new DocumentoYaRegistradoException();
        }
    }
}
