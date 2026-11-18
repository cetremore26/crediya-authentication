package co.com.crediya.model.user;

import co.com.crediya.model.user.exception.UserValidationException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.Builder;
import lombok.Getter;

/**
 * Usuario del sistema. Además de los datos, conoce sus propias reglas:
 * cómo se normaliza ({@link #normalized()}) y cuándo es válido ({@link #validate()}).
 * Es inmutable: los cambios se hacen con {@code toBuilder()}.
 */
@Getter
@Builder(toBuilder = true)
public class User {
    private static final int MINIMUM_PASSWORD_LENGTH = 8;
    /** BCrypt solo usa los primeros 72 bytes de la clave. */
    private static final int MAXIMUM_PASSWORD_BYTES = 72;
    private static final int MAXIMUM_NAME_LENGTH = 150;
    private static final int MAXIMUM_ADDRESS_LENGTH = 300;
    private static final int MAXIMUM_PHONE_LENGTH = 50;
    private static final int MAXIMUM_EMAIL_LENGTH = 320;
    private static final BigDecimal MINIMUM_SALARY = BigDecimal.ZERO;
    private static final BigDecimal MAXIMUM_SALARY = new BigDecimal("15000000");
    private static final Pattern DOCUMENTO_PATTERN = Pattern.compile("^\\d{5,15}$");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private UUID id;
    private String documentoIdentidad;
    private String nombres;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private String direccion;
    private String telefono;
    private String correoElectronico;
    private BigDecimal salarioBase;
    private String clave;
    private Rol rol;

    /**
     * Devuelve una copia con los datos limpios: correo sin espacios y en minúsculas,
     * documento sin espacios (o null si viene vacío) y rol CLIENTE por defecto.
     */
    public User normalized() {
        return toBuilder()
                .correoElectronico(normalizeEmail(correoElectronico))
                .documentoIdentidad(isBlank(documentoIdentidad) ? null : documentoIdentidad.trim())
                .rol(rol == null ? Rol.CLIENTE : rol)
                .build();
    }

    /** Correo sin espacios y en minúsculas: la forma en que se guarda y se busca. */
    public static String normalizeEmail(String correoElectronico) {
        return correoElectronico == null ? null : correoElectronico.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Verifica que el usuario cumpla las reglas del dominio. Se debe llamar sobre
     * un usuario ya normalizado.
     *
     * @throws UserValidationException con el primer dato inválido encontrado
     */
    public void validate() {
        if (isBlank(nombres)) {
            throw new UserValidationException("Los nombres son obligatorios.");
        }
        if (isBlank(apellidos)) {
            throw new UserValidationException("Los apellidos son obligatorios.");
        }
        ensureMaximumLength(nombres, MAXIMUM_NAME_LENGTH, "nombres");
        ensureMaximumLength(apellidos, MAXIMUM_NAME_LENGTH, "apellidos");
        ensureMaximumLength(direccion, MAXIMUM_ADDRESS_LENGTH, "direccion");
        ensureMaximumLength(telefono, MAXIMUM_PHONE_LENGTH, "telefono");
        ensureMaximumLength(correoElectronico, MAXIMUM_EMAIL_LENGTH, "correo_electronico");
        if (documentoIdentidad != null && !DOCUMENTO_PATTERN.matcher(documentoIdentidad).matches()) {
            throw new UserValidationException(
                    "El documento de identidad debe contener entre 5 y 15 dígitos.");
        }
        if (rol == Rol.CLIENTE && documentoIdentidad == null) {
            throw new UserValidationException(
                    "El documento de identidad es obligatorio para los clientes.");
        }
        if (clave == null
                || clave.length() < MINIMUM_PASSWORD_LENGTH
                || clave.getBytes(StandardCharsets.UTF_8).length > MAXIMUM_PASSWORD_BYTES) {
            throw new UserValidationException(
                    "La clave debe tener mínimo 8 caracteres y máximo 72 bytes "
                            + "(72 caracteres sin tildes ni símbolos especiales).");
        }
        if (isBlank(correoElectronico) || !EMAIL_PATTERN.matcher(correoElectronico).matches()) {
            throw new UserValidationException("El correo electrónico no es válido.");
        }
        if (salarioBase == null
                || salarioBase.compareTo(MINIMUM_SALARY) < 0
                || salarioBase.compareTo(MAXIMUM_SALARY) > 0) {
            throw new UserValidationException("El salario base debe estar entre 0 y 15000000.");
        }
    }

    private static void ensureMaximumLength(String value, int maximum, String field) {
        if (value != null && value.length() > maximum) {
            throw new UserValidationException(
                    "El campo '" + field + "' no puede superar " + maximum + " caracteres.");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
