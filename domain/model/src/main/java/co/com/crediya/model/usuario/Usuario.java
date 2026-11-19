package co.com.crediya.model.usuario;

import co.com.crediya.model.usuario.exception.UsuarioInvalidoException;
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
 * cómo se normaliza ({@link #normalizado()}) y cuándo es válido ({@link #validar()}).
 * Es inmutable: los cambios se hacen con {@code toBuilder()}.
 */
@Getter
@Builder(toBuilder = true)
public class Usuario {
    private static final int LONGITUD_MINIMA_CLAVE = 8;
    /** BCrypt solo usa los primeros 72 bytes de la clave. */
    private static final int BYTES_MAXIMOS_CLAVE = 72;
    private static final int LONGITUD_MAXIMA_NOMBRE = 150;
    private static final int LONGITUD_MAXIMA_DIRECCION = 300;
    private static final int LONGITUD_MAXIMA_TELEFONO = 50;
    private static final int LONGITUD_MAXIMA_CORREO = 320;
    private static final BigDecimal SALARIO_MINIMO = BigDecimal.ZERO;
    private static final BigDecimal SALARIO_MAXIMO = new BigDecimal("15000000");
    private static final Pattern PATRON_DOCUMENTO = Pattern.compile("^\\d{5,15}$");
    private static final Pattern PATRON_CORREO =
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
    public Usuario normalizado() {
        return toBuilder()
                .correoElectronico(normalizarCorreo(correoElectronico))
                .documentoIdentidad(estaVacio(documentoIdentidad) ? null : documentoIdentidad.trim())
                .rol(rol == null ? Rol.CLIENTE : rol)
                .build();
    }

    /** Correo sin espacios y en minúsculas: la forma en que se guarda y se busca. */
    public static String normalizarCorreo(String correoElectronico) {
        return correoElectronico == null ? null : correoElectronico.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Verifica que el usuario cumpla las reglas del dominio. Se debe llamar sobre
     * un usuario ya normalizado.
     *
     * @throws UsuarioInvalidoException con el primer dato inválido encontrado
     */
    public void validar() {
        if (estaVacio(nombres)) {
            throw new UsuarioInvalidoException("Los nombres son obligatorios.");
        }
        if (estaVacio(apellidos)) {
            throw new UsuarioInvalidoException("Los apellidos son obligatorios.");
        }
        validarLongitudMaxima(nombres, LONGITUD_MAXIMA_NOMBRE, "nombres");
        validarLongitudMaxima(apellidos, LONGITUD_MAXIMA_NOMBRE, "apellidos");
        validarLongitudMaxima(direccion, LONGITUD_MAXIMA_DIRECCION, "direccion");
        validarLongitudMaxima(telefono, LONGITUD_MAXIMA_TELEFONO, "telefono");
        validarLongitudMaxima(correoElectronico, LONGITUD_MAXIMA_CORREO, "correo_electronico");
        if (documentoIdentidad != null && !PATRON_DOCUMENTO.matcher(documentoIdentidad).matches()) {
            throw new UsuarioInvalidoException(
                    "El documento de identidad debe contener entre 5 y 15 dígitos.");
        }
        if (rol == Rol.CLIENTE && documentoIdentidad == null) {
            throw new UsuarioInvalidoException(
                    "El documento de identidad es obligatorio para los clientes.");
        }
        if (clave == null
                || clave.length() < LONGITUD_MINIMA_CLAVE
                || clave.getBytes(StandardCharsets.UTF_8).length > BYTES_MAXIMOS_CLAVE) {
            throw new UsuarioInvalidoException(
                    "La clave debe tener mínimo 8 caracteres y máximo 72 bytes "
                            + "(72 caracteres sin tildes ni símbolos especiales).");
        }
        if (estaVacio(correoElectronico) || !PATRON_CORREO.matcher(correoElectronico).matches()) {
            throw new UsuarioInvalidoException("El correo electrónico no es válido.");
        }
        if (salarioBase == null
                || salarioBase.compareTo(SALARIO_MINIMO) < 0
                || salarioBase.compareTo(SALARIO_MAXIMO) > 0) {
            throw new UsuarioInvalidoException("El salario base debe estar entre 0 y 15000000.");
        }
    }

    private static void validarLongitudMaxima(String valor, int maximo, String campo) {
        if (valor != null && valor.length() > maximo) {
            throw new UsuarioInvalidoException(
                    "El campo '" + campo + "' no puede superar " + maximo + " caracteres.");
        }
    }

    private static boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
