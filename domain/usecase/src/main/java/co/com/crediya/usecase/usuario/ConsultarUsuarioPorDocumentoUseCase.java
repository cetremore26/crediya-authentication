package co.com.crediya.usecase.usuario;

import co.com.crediya.model.autenticacion.UsuarioAutenticado;
import co.com.crediya.model.autenticacion.exception.AccesoDenegadoException;
import co.com.crediya.model.usuario.Usuario;
import co.com.crediya.model.usuario.exception.UsuarioNoEncontradoException;
import co.com.crediya.model.usuario.gateways.UsuarioRepository;

/**
 * Consulta un usuario por documento. La regla de quién puede consultar a quién vive en
 * {@link UsuarioAutenticado#puedeConsultarDocumento(String)}.
 */
public class ConsultarUsuarioPorDocumentoUseCase {
    private final UsuarioRepository usuarioRepository;

    public ConsultarUsuarioPorDocumentoUseCase(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * @throws AccesoDenegadoException cuando el solicitante no puede consultar ese documento;
     *         se valida antes de buscar para no revelar si el documento existe
     * @throws UsuarioNoEncontradoException cuando no hay un usuario con ese documento
     */
    public Usuario execute(String documentoIdentidad, UsuarioAutenticado solicitante) {
        if (solicitante == null || !solicitante.puedeConsultarDocumento(documentoIdentidad)) {
            throw new AccesoDenegadoException("Solo puede consultar sus propios datos.");
        }
        return usuarioRepository.buscarPorDocumentoIdentidad(documentoIdentidad)
                .orElseThrow(UsuarioNoEncontradoException::new);
    }
}
