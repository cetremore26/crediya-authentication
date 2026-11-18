package co.com.crediya.usecase.usuario;

import co.com.crediya.model.autenticacion.UsuarioAutenticado;
import co.com.crediya.model.autenticacion.exception.AccesoDenegadoException;
import co.com.crediya.model.usuario.Usuario;
import co.com.crediya.model.usuario.exception.UsuarioInvalidoException;
import co.com.crediya.model.usuario.gateways.UsuarioRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Consulta por lote los usuarios de varios documentos, para que el personal revise solicitudes.
 * Solo el personal puede usarla ({@link UsuarioAutenticado#esPersonal()}).
 */
public class ConsultarUsuariosPorDocumentosUseCase {
    private static final int MAXIMO_DOCUMENTOS = 100;

    private final UsuarioRepository usuarioRepository;

    public ConsultarUsuariosPorDocumentosUseCase(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Devuelve los usuarios registrados de los documentos indicados; omite los que no existan.
     *
     * @throws AccesoDenegadoException si el solicitante no es personal de CrediYa
     */
    public List<Usuario> execute(Collection<String> documentos, UsuarioAutenticado solicitante) {
        if (solicitante == null || !solicitante.esPersonal()) {
            throw new AccesoDenegadoException("Solo el personal de CrediYa puede consultar usuarios por lote.");
        }
        if (documentos == null || documentos.isEmpty()) {
            throw new UsuarioInvalidoException("Debe indicar al menos un documento.");
        }
        if (documentos.size() > MAXIMO_DOCUMENTOS) {
            throw new UsuarioInvalidoException(
                    "No se pueden consultar más de " + MAXIMO_DOCUMENTOS + " documentos a la vez.");
        }
        return documentos.stream()
                .map(String::trim)
                .distinct()
                .map(usuarioRepository::buscarPorDocumentoIdentidad)
                .flatMap(Optional::stream)
                .toList();
    }
}
