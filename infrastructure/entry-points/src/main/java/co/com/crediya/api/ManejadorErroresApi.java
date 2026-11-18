package co.com.crediya.api;

import co.com.crediya.model.autenticacion.exception.AccesoDenegadoException;
import co.com.crediya.model.autenticacion.exception.CredencialesInvalidasException;
import co.com.crediya.model.usuario.exception.DocumentoYaRegistradoException;
import co.com.crediya.model.usuario.exception.RolNoPermitidoException;
import co.com.crediya.model.usuario.exception.UsuarioYaRegistradoException;
import co.com.crediya.model.usuario.exception.UsuarioNoEncontradoException;
import co.com.crediya.model.usuario.exception.UsuarioInvalidoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ManejadorErroresApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManejadorErroresApi.class);

    @ExceptionHandler(UsuarioInvalidoException.class)
    public ResponseEntity<ErrorApiResponse> manejarValidacion(UsuarioInvalidoException exception) {
        LOGGER.warn("Validación fallida: {}", exception.getMessage());
        return respuestaError(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", exception.getMessage());
    }

    @ExceptionHandler(DocumentoYaRegistradoException.class)
    public ResponseEntity<ErrorApiResponse> manejarDocumentoDuplicado(
            DocumentoYaRegistradoException exception) {
        LOGGER.warn("Intento de registro con documento duplicado.");
        return respuestaError(HttpStatus.CONFLICT, "DOCUMENTO_DUPLICADO", exception.getMessage());
    }

    @ExceptionHandler(UsuarioYaRegistradoException.class)
    public ResponseEntity<ErrorApiResponse> manejarCorreoDuplicado(UsuarioYaRegistradoException exception) {
        LOGGER.warn("Intento de registro con correo duplicado.");
        return respuestaError(HttpStatus.CONFLICT, "CORREO_DUPLICADO", exception.getMessage());
    }

    @ExceptionHandler(RolNoPermitidoException.class)
    public ResponseEntity<ErrorApiResponse> manejarRolNoPermitido(RolNoPermitidoException exception) {
        LOGGER.warn("Registro rechazado: el solicitante no puede asignar el rol pedido.");
        return respuestaError(HttpStatus.FORBIDDEN, "ROL_NO_PERMITIDO", exception.getMessage());
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ErrorApiResponse> manejarAccesoDenegado(AccesoDenegadoException exception) {
        LOGGER.warn("Operación rechazada: el usuario no tiene permiso.");
        return respuestaError(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO", exception.getMessage());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorApiResponse> manejarCredencialesInvalidas(
            CredencialesInvalidasException exception) {
        LOGGER.warn("Intento de inicio de sesión con credenciales inválidas.");
        return respuestaError(HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS", exception.getMessage());
    }

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<ErrorApiResponse> manejarNoEncontrado(UsuarioNoEncontradoException exception) {
        LOGGER.warn("Consulta de usuario inexistente.");
        return respuestaError(HttpStatus.NOT_FOUND, "USUARIO_NO_ENCONTRADO", exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorApiResponse> manejarCuerpoInvalido(
            HttpMessageNotReadableException exception) {
        LOGGER.warn("Cuerpo de la solicitud con formato inválido.");
        return respuestaError(
                HttpStatus.BAD_REQUEST,
                "SOLICITUD_INVALIDA",
                "El cuerpo de la solicitud no tiene un formato válido.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorApiResponse> manejarParametroInvalido(
            MethodArgumentTypeMismatchException exception) {
        LOGGER.warn("Parámetro inválido: {}", exception.getName());
        return respuestaError(
                HttpStatus.BAD_REQUEST,
                "PARAMETRO_INVALIDO",
                "El valor del parámetro '" + exception.getName() + "' no es válido.");
    }

    /**
     * Errores que no esperamos. Las excepciones propias de Spring MVC (ruta inexistente,
     * método o tipo de contenido no soportado, etc.) traen su estado HTTP y se responden con él
     * en vez de un 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorApiResponse> manejarInesperado(Exception exception) {
        if (exception instanceof ErrorResponse errorResponse) {
            return manejarErrorDelFramework(errorResponse);
        }
        LOGGER.error("Error inesperado al procesar la solicitud.", exception);
        return respuestaError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "ERROR_INTERNO",
                "Ocurrió un error interno. Intente nuevamente más tarde.");
    }

    private ResponseEntity<ErrorApiResponse> manejarErrorDelFramework(ErrorResponse errorResponse) {
        HttpStatusCode status = errorResponse.getStatusCode();
        LOGGER.warn("Solicitud HTTP no soportada: {}", status);
        return ResponseEntity.status(status)
                .body(new ErrorApiResponse(
                        status.value(),
                        "SOLICITUD_NO_SOPORTADA",
                        "La solicitud no puede ser procesada por este servicio."));
    }

    private ResponseEntity<ErrorApiResponse> respuestaError(
            HttpStatus status, String code, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorApiResponse(status.value(), code, message));
    }
}
