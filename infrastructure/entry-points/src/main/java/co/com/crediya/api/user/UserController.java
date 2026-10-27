package co.com.crediya.api.user;

import co.com.crediya.usecase.user.RegisterUserUseCase;
import co.com.crediya.usecase.user.exception.UserValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UserController {
    private final RegisterUserUseCase registerUserUseCase;

    public UserController(RegisterUserUseCase registerUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
    }

    @PostMapping
    public ResponseEntity<UserResponse> register(@RequestBody UserRequest request) {
        if (request == null) {
            throw new UserValidationException("El cuerpo de la solicitud es obligatorio.");
        }
        registerUserUseCase.execute(request.toDomain());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.registered());
    }
}
