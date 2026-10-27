package co.com.crediya.api;

import co.com.crediya.model.user.exception.UserAlreadyExistsException;
import co.com.crediya.usecase.user.exception.UserValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(UserValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(UserValidationException exception) {
        LOGGER.warn("Validación fallida al registrar usuario: {}", exception.getMessage());
        return error(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", exception.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicate(UserAlreadyExistsException exception) {
        LOGGER.warn("Intento de registro con correo duplicado.");
        return error(HttpStatus.CONFLICT, "CORREO_DUPLICADO", exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedRequest(
            HttpMessageNotReadableException exception) {
        LOGGER.warn("Cuerpo de la solicitud con formato inválido.");
        return error(
                HttpStatus.BAD_REQUEST,
                "SOLICITUD_INVALIDA",
                "El cuerpo de la solicitud no tiene un formato válido.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception) {
        LOGGER.error("Error inesperado al procesar la solicitud de registro de usuario.", exception);
        return error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "ERROR_INTERNO",
                "Ocurrió un error interno. Intente nuevamente más tarde.");
    }

    private ResponseEntity<ApiErrorResponse> error(
            HttpStatus status, String code, String message) {
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(status.value(), code, message));
    }
}
