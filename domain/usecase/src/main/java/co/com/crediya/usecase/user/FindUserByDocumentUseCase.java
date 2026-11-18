package co.com.crediya.usecase.user;

import co.com.crediya.model.auth.AuthenticatedUser;
import co.com.crediya.model.auth.exception.AccesoDenegadoException;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.exception.UserNotFoundException;
import co.com.crediya.model.user.gateways.UserRepository;

/**
 * Consulta un usuario por documento. La regla de quién puede consultar a quién vive en
 * {@link AuthenticatedUser#puedeConsultarDocumento(String)}.
 */
public class FindUserByDocumentUseCase {
    private final UserRepository userRepository;

    public FindUserByDocumentUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * @throws AccesoDenegadoException cuando el solicitante no puede consultar ese documento;
     *         se valida antes de buscar para no revelar si el documento existe
     * @throws UserNotFoundException cuando no hay un usuario con ese documento
     */
    public User execute(String documentoIdentidad, AuthenticatedUser solicitante) {
        if (solicitante == null || !solicitante.puedeConsultarDocumento(documentoIdentidad)) {
            throw new AccesoDenegadoException("Solo puede consultar sus propios datos.");
        }
        return userRepository.findByDocumentoIdentidad(documentoIdentidad)
                .orElseThrow(UserNotFoundException::new);
    }
}
