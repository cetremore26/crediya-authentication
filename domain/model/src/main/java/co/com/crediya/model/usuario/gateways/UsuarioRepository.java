package co.com.crediya.model.usuario.gateways;

import co.com.crediya.model.usuario.Usuario;
import co.com.crediya.model.usuario.exception.UsuarioYaRegistradoException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Persistencia de usuarios. Solo guarda y consulta: recibe los usuarios ya normalizados y
 * validados por el dominio ({@link Usuario#normalizado()}, {@link Usuario#validar()}) y devuelve
 * los datos completos; decidir qué se expone hacia afuera es tarea de quien los use.
 * Los correos se reciben normalizados ({@link Usuario#normalizarCorreo(String)}), así que se
 * comparan tal cual y la búsqueda puede usar el índice único.
 */
public interface UsuarioRepository {
    boolean existePorCorreoElectronico(String correoElectronico);

    Optional<Usuario> buscarPorCorreoElectronico(String correoElectronico);

    boolean existePorDocumentoIdentidad(String documentoIdentidad);

    Optional<Usuario> buscarPorDocumentoIdentidad(String documentoIdentidad);

    /** Usuarios de los documentos indicados, en una sola consulta; omite los que no existan. */
    List<Usuario> buscarPorDocumentosIdentidad(Collection<String> documentosIdentidad);

    /**
     * Guarda un usuario o falla si el correo o el documento ya están registrados.
     *
     * @throws UsuarioYaRegistradoException si otro usuario tiene el mismo correo o documento
     */
    Usuario guardar(Usuario usuario);
}
