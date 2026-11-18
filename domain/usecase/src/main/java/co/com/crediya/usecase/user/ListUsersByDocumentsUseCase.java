package co.com.crediya.usecase.user;

import co.com.crediya.model.auth.AuthenticatedUser;
import co.com.crediya.model.auth.exception.AccesoDenegadoException;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.exception.UserValidationException;
import co.com.crediya.model.user.gateways.UserRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Consulta por lote los usuarios de varios documentos, para que el personal revise solicitudes.
 * Solo el personal puede usarla ({@link AuthenticatedUser#esPersonal()}).
 */
public class ListUsersByDocumentsUseCase {
    private static final int MAXIMUM_DOCUMENTS = 100;

    private final UserRepository userRepository;

    public ListUsersByDocumentsUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns the registered users for the given documents; unknown documents are skipped.
     *
     * @throws AccesoDenegadoException when the requester is not staff
     */
    public List<User> execute(Collection<String> documentos, AuthenticatedUser solicitante) {
        if (solicitante == null || !solicitante.esPersonal()) {
            throw new AccesoDenegadoException("Solo el personal de CrediYa puede consultar usuarios por lote.");
        }
        if (documentos == null || documentos.isEmpty()) {
            throw new UserValidationException("Debe indicar al menos un documento.");
        }
        if (documentos.size() > MAXIMUM_DOCUMENTS) {
            throw new UserValidationException(
                    "No se pueden consultar más de " + MAXIMUM_DOCUMENTS + " documentos a la vez.");
        }
        return documentos.stream()
                .map(String::trim)
                .distinct()
                .map(userRepository::findByDocumentoIdentidad)
                .flatMap(Optional::stream)
                .toList();
    }
}
