package co.com.crediya.usecase.user;

import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import co.com.crediya.usecase.user.exception.UserValidationException;
import java.util.Collection;
import java.util.List;

public class ListUsersByDocumentsUseCase {
    private static final int MAXIMUM_DOCUMENTS = 100;

    private final UserRepository userRepository;

    public ListUsersByDocumentsUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Returns the registered users for the given documents; unknown documents are skipped. */
    public List<User> execute(Collection<String> documentos) {
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
                .flatMap(java.util.Optional::stream)
                .toList();
    }
}
