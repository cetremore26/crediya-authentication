package co.com.crediya.usecase.usuario;

import co.com.crediya.model.autenticacion.UsuarioAutenticado;
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
 * válido viven en {@link Usuario} y las de quién puede asignar cada rol en {@link Rol}
 * ({@link UsuarioAutenticado#puedeAsignarRol(Rol)}).
 */
@RequiredArgsConstructor
public class RegistrarUsuarioUseCase {
    private final UsuarioRepository usuarioRepository;
    private final CifradorClaves cifradorClaves;

    /**
     * @param usuario datos del usuario a registrar
     * @param solicitante quien hace el registro
     * @throws RolNoPermitidoException cuando el solicitante no puede asignar el rol del usuario nuevo
     */
    public Usuario execute(Usuario usuario, UsuarioAutenticado solicitante) {
        Usuario candidato = normalizar(usuario);
        if (solicitante == null || !solicitante.puedeAsignarRol(candidato.getRol())) {
            throw new RolNoPermitidoException(candidato.getRol());
        }
        return registrar(candidato);
    }

    /**
     * Crea el administrador inicial si todavía no hay un usuario con su correo. Es el único
     * registro sin solicitante: lo hace el arranque, cuando aún no existe nadie que pueda hacerlo.
     * Primero comprueba si ya existe, así que un administrador ya creado no se vuelve a validar.
     *
     * @return {@code true} si lo creó, {@code false} si ya existía
     */
    public boolean registrarAdministradorInicial(Usuario administrador) {
        Usuario candidato = normalizar(administrador).toBuilder().rol(Rol.ADMIN).build();
        if (usuarioRepository.existePorCorreoElectronico(candidato.getCorreoElectronico())) {
            return false;
        }
        try {
            registrar(candidato);
            return true;
        } catch (UsuarioYaRegistradoException exception) {
            // Otra instancia lo creó entre la comprobación y el guardado.
            return false;
        }
    }

    private static Usuario normalizar(Usuario usuario) {
        if (usuario == null) {
            throw new UsuarioInvalidoException("El usuario es obligatorio.");
        }
        return usuario.normalizado();
    }

    private Usuario registrar(Usuario candidato) {
        candidato.validar();
        validarNoRegistrado(candidato);
        return usuarioRepository.guardar(candidato.toBuilder()
                .clave(cifradorClaves.cifrar(candidato.getClave()))
                .build());
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
