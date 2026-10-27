package co.com.crediya.usecase.user;

import co.com.crediya.model.user.User;
import co.com.crediya.model.user.exception.UserNotFoundException;
import co.com.crediya.model.user.gateways.UserRepository;

public class FindUserByDocumentUseCase {
    private final UserRepository userRepository;

    public FindUserByDocumentUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User execute(String documentoIdentidad) {
        return userRepository.findByDocumentoIdentidad(documentoIdentidad)
                .orElseThrow(UserNotFoundException::new);
    }
}
