package co.com.crediya.usecase.usuario;

import co.com.crediya.model.autenticacion.TokenAcceso;
import co.com.crediya.model.autenticacion.exception.CredencialesInvalidasException;
import co.com.crediya.model.autenticacion.gateways.CifradorClaves;
import co.com.crediya.model.autenticacion.gateways.EmisorTokens;
import co.com.crediya.model.usuario.Usuario;
import co.com.crediya.model.usuario.exception.UsuarioInvalidoException;
import co.com.crediya.model.usuario.gateways.UsuarioRepository;
import java.util.Optional;

/**
 * Inicia sesión. Solo orquesta: busca al usuario, delega la verificación de la clave en
 * {@link CifradorClaves} y la emisión del token en {@link EmisorTokens}.
 *
 * <p>La clave se verifica siempre, también cuando el correo no existe (contra un hash de
 * referencia): así la respuesta tarda lo mismo en ambos casos y no revela qué correos están
 * registrados.
 */
public class IniciarSesionUseCase {
    private static final String CLAVE_DE_REFERENCIA = "clave-de-referencia-sin-usuario";

    private final UsuarioRepository usuarioRepository;
    private final CifradorClaves cifradorClaves;
    private final EmisorTokens emisorTokens;
    private final String hashDeReferencia;

    public IniciarSesionUseCase(
            UsuarioRepository usuarioRepository, CifradorClaves cifradorClaves, EmisorTokens emisorTokens) {
        this.usuarioRepository = usuarioRepository;
        this.cifradorClaves = cifradorClaves;
        this.emisorTokens = emisorTokens;
        this.hashDeReferencia = cifradorClaves.cifrar(CLAVE_DE_REFERENCIA);
    }

    public TokenAcceso execute(String correoElectronico, String clave) {
        if (estaVacio(correoElectronico) || estaVacio(clave)) {
            throw new UsuarioInvalidoException("El correo y la clave son obligatorios.");
        }

        Optional<Usuario> usuario = usuarioRepository
                .buscarPorCorreoElectronico(Usuario.normalizarCorreo(correoElectronico))
                .filter(found -> found.getClave() != null);
        boolean claveCorrecta =
                cifradorClaves.coincide(clave, usuario.map(Usuario::getClave).orElse(hashDeReferencia));

        if (usuario.isEmpty() || !claveCorrecta) {
            throw new CredencialesInvalidasException();
        }
        return emisorTokens.emitir(usuario.get());
    }

    private static boolean estaVacio(String value) {
        return value == null || value.isBlank();
    }
}
