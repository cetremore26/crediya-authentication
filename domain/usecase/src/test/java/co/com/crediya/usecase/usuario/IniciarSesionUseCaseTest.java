package co.com.crediya.usecase.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.com.crediya.model.autenticacion.TokenAcceso;
import co.com.crediya.model.autenticacion.exception.CredencialesInvalidasException;
import co.com.crediya.model.autenticacion.gateways.CifradorClaves;
import co.com.crediya.model.autenticacion.gateways.EmisorTokens;
import co.com.crediya.model.usuario.Rol;
import co.com.crediya.model.usuario.Usuario;
import co.com.crediya.model.usuario.gateways.UsuarioRepository;
import co.com.crediya.model.usuario.exception.UsuarioInvalidoException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class IniciarSesionUseCaseTest {
    private static final String EMAIL = "ana@example.com";
    private static final String PASSWORD = "Clave1234";

    private final Usuario usuarioGuardado = Usuario.builder()
            .correoElectronico(EMAIL)
            .clave("hash:" + PASSWORD)
            .rol(Rol.CLIENTE)
            .build();
    private final UsuarioRepository usuarioRepository = new UsuarioRepositoryFalso(usuarioGuardado);
    private final CifradorClaves cifradorClaves = new CifradorClaves() {
        @Override
        public String cifrar(String claveEnClaro) {
            return "hash:" + claveEnClaro;
        }

        @Override
        public boolean coincide(String claveEnClaro, String claveCifrada) {
            return cifrar(claveEnClaro).equals(claveCifrada);
        }
    };
    private final EmisorTokens emisorTokens = usuario -> new TokenAcceso("token-" + usuario.getRol(), 3600);
    private final IniciarSesionUseCase useCase = new IniciarSesionUseCase(usuarioRepository, cifradorClaves, emisorTokens);

    @Test
    void returnsTokenForValidCredentialsIgnoringEmailCase() {
        TokenAcceso token = useCase.execute("  ANA@Example.com ", PASSWORD);

        assertEquals("token-CLIENTE", token.token());
    }

    @Test
    void rejectsWrongPassword() {
        assertThrows(CredencialesInvalidasException.class, () -> useCase.execute(EMAIL, "otra-clave"));
    }

    @Test
    void rejectsUnknownEmail() {
        assertThrows(
                CredencialesInvalidasException.class, () -> useCase.execute("nadie@example.com", PASSWORD));
    }

    @Test
    void verifiesAPasswordEvenWhenTheEmailIsUnknownSoTimingDoesNotRevealIt() {
        List<String> clavesVerificadas = new ArrayList<>();
        CifradorClaves cifradorQueRegistra = new CifradorClaves() {
            @Override
            public String cifrar(String claveEnClaro) {
                return cifradorClaves.cifrar(claveEnClaro);
            }

            @Override
            public boolean coincide(String claveEnClaro, String claveCifrada) {
                clavesVerificadas.add(claveCifrada);
                return cifradorClaves.coincide(claveEnClaro, claveCifrada);
            }
        };
        IniciarSesionUseCase iniciarSesionUseCase = new IniciarSesionUseCase(usuarioRepository, cifradorQueRegistra, emisorTokens);

        assertThrows(CredencialesInvalidasException.class,
                () -> iniciarSesionUseCase.execute("nadie@example.com", PASSWORD));
        assertThrows(CredencialesInvalidasException.class, () -> iniciarSesionUseCase.execute(EMAIL, "otra"));

        assertEquals(2, clavesVerificadas.size());
    }

    @Test
    void rejectsUsersWithoutPassword() {
        UsuarioRepository repository = new UsuarioRepositoryFalso(usuarioGuardado.toBuilder().clave(null).build());
        IniciarSesionUseCase iniciarSesionUseCase = new IniciarSesionUseCase(repository, cifradorClaves, emisorTokens);

        assertThrows(CredencialesInvalidasException.class, () -> iniciarSesionUseCase.execute(EMAIL, PASSWORD));
    }

    @Test
    void rejectsBlankFields() {
        assertThrows(UsuarioInvalidoException.class, () -> useCase.execute(" ", PASSWORD));
        assertThrows(UsuarioInvalidoException.class, () -> useCase.execute(EMAIL, null));
    }

    private record UsuarioRepositoryFalso(Usuario usuario) implements UsuarioRepository {
        @Override
        public boolean existePorCorreoElectronico(String correoElectronico) {
            return false;
        }

        @Override
        public Optional<Usuario> buscarPorCorreoElectronico(String correoElectronico) {
            return Optional.of(usuario).filter(found -> found.getCorreoElectronico().equals(correoElectronico));
        }

        @Override
        public boolean existePorDocumentoIdentidad(String documentoIdentidad) {
            return false;
        }

        @Override
        public Optional<Usuario> buscarPorDocumentoIdentidad(String documentoIdentidad) {
            return Optional.empty();
        }

        @Override
        public Usuario guardar(Usuario usuario) {
            return usuario;
        }
    }
}
