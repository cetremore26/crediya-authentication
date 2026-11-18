package co.com.crediya.model.usuario.gateways;

import co.com.crediya.model.usuario.Usuario;
import co.com.crediya.model.usuario.exception.UsuarioYaRegistradoException;
import java.util.Optional;

/**
 * Persistencia de usuarios. Solo guarda y consulta: recibe los usuarios ya normalizados y
 * validados por el dominio ({@link Usuario#normalizado()}, {@link Usuario#validar()}) y devuelve
 * los datos completos; decidir qué se expone hacia afuera es tarea de quien los use.
 */
public interface UsuarioRepository {
    boolean existePorCorreoElectronico(String correoElectronico);

    Optional<Usuario> buscarPorCorreoElectronico(String correoElectronico);

    boolean existePorDocumentoIdentidad(String documentoIdentidad);

    Optional<Usuario> buscarPorDocumentoIdentidad(String documentoIdentidad);

    /**
     * Guarda un usuario o falla si el correo o el documento ya están registrados.
     *
     * @throws UsuarioYaRegistradoException si otro usuario tiene el mismo correo o documento
     */
    Usuario guardar(Usuario usuario);
}
