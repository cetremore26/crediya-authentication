package co.com.crediya.usecase.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.com.crediya.model.autenticacion.UsuarioAutenticado;
import co.com.crediya.model.autenticacion.gateways.CifradorClaves;
import co.com.crediya.model.usuario.Rol;
import co.com.crediya.model.usuario.Usuario;
import co.com.crediya.model.usuario.exception.DocumentoYaRegistradoException;
import co.com.crediya.model.usuario.exception.RolNoPermitidoException;
import co.com.crediya.model.usuario.exception.UsuarioYaRegistradoException;
import co.com.crediya.model.usuario.gateways.UsuarioRepository;
import co.com.crediya.model.usuario.exception.UsuarioInvalidoException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegistrarUsuarioUseCaseTest {
    private final UsuarioRepositoryFalso repository = new UsuarioRepositoryFalso();
    private final RegistrarUsuarioUseCase useCase = new RegistrarUsuarioUseCase(repository, new CifradorClavesFalso());

    @Test
    void registersUserAndTrimsEmail() {
        Usuario usuario = usuarioValido().toBuilder()
                .correoElectronico("  solicitante@example.com  ")
                .build();

        Usuario saved = useCase.execute(usuario, solicitante(Rol.ADMIN));

        assertEquals("solicitante@example.com", saved.getCorreoElectronico());
        assertEquals(saved, repository.usuariosGuardados.get(0));
    }

    @Test
    void rejectsNullUser() {
        assertThrows(UsuarioInvalidoException.class, () -> useCase.execute(null, solicitante(Rol.ADMIN)));
    }

    @Test
    void rejectsMissingRequiredFields() {
        List<Usuario> usuariosInvalidos = List.of(
                usuarioValido().toBuilder().nombres(" ").build(),
                usuarioValido().toBuilder().apellidos(null).build(),
                usuarioValido().toBuilder().correoElectronico("").build(),
                usuarioValido().toBuilder().salarioBase(null).build());

        for (Usuario usuario : usuariosInvalidos) {
            assertThrows(UsuarioInvalidoException.class, () -> useCase.execute(usuario, solicitante(Rol.ADMIN)));
        }
        assertEquals(0, repository.usuariosGuardados.size());
    }

    @Test
    void rejectsInvalidEmail() {
        Usuario usuario = usuarioValido().toBuilder()
                .correoElectronico("correo-invalido")
                .build();

        assertThrows(UsuarioInvalidoException.class, () -> useCase.execute(usuario, solicitante(Rol.ADMIN)));
        assertEquals(0, repository.usuariosGuardados.size());
    }

    @Test
    void rejectsSalaryOutsideAllowedRange() {
        List<Usuario> usuariosInvalidos = List.of(
                usuarioValido().toBuilder().salarioBase(new BigDecimal("-0.01")).build(),
                usuarioValido().toBuilder().salarioBase(new BigDecimal("15000000.01")).build());

        for (Usuario usuario : usuariosInvalidos) {
            assertThrows(UsuarioInvalidoException.class, () -> useCase.execute(usuario, solicitante(Rol.ADMIN)));
        }
        assertEquals(0, repository.usuariosGuardados.size());
    }

    @Test
    void acceptsSalaryAtBothRangeBoundaries() {
        useCase.execute(usuarioValido().toBuilder().salarioBase(BigDecimal.ZERO).build(), solicitante(Rol.ADMIN));
        useCase.execute(usuarioValido().toBuilder()
                .salarioBase(new BigDecimal("15000000"))
                .build(), solicitante(Rol.ADMIN));

        assertEquals(2, repository.usuariosGuardados.size());
    }

    @Test
    void rejectsAlreadyRegisteredEmail() {
        repository.emailAlreadyExists = true;

        assertThrows(UsuarioYaRegistradoException.class, () -> useCase.execute(usuarioValido(), solicitante(Rol.ADMIN)));
        assertEquals(0, repository.usuariosGuardados.size());
    }

    @Test
    void registersUserWithValidDocument() {
        Usuario saved = useCase.execute(
                usuarioValido().toBuilder().documentoIdentidad(" 1020304050 ").build(), solicitante(Rol.ADMIN));

        assertEquals("1020304050", saved.getDocumentoIdentidad());
    }

    @Test
    void rejectsInvalidDocumentFormat() {
        Usuario usuario = usuarioValido().toBuilder().documentoIdentidad("12ab").build();

        assertThrows(UsuarioInvalidoException.class, () -> useCase.execute(usuario, solicitante(Rol.ADMIN)));
    }

    @Test
    void rejectsAlreadyRegisteredDocument() {
        repository.documentAlreadyExists = true;
        Usuario usuario = usuarioValido().toBuilder().documentoIdentidad("1020304050").build();

        assertThrows(DocumentoYaRegistradoException.class, () -> useCase.execute(usuario, solicitante(Rol.ADMIN)));
        assertEquals(0, repository.usuariosGuardados.size());
    }

    @Test
    void lowercasesEmailBeforeSaving() {
        Usuario saved = useCase.execute(usuarioValido().toBuilder()
                .correoElectronico("Ana@Example.COM")
                .build(), solicitante(Rol.ADMIN));

        assertEquals("ana@example.com", saved.getCorreoElectronico());
    }

    @Test
    void createsTheInitialAdministratorWhenItDoesNotExist() {
        assertTrue(useCase.registrarAdministradorInicial(usuarioValido()));

        assertEquals(Rol.ADMIN, repository.usuariosGuardados.get(0).getRol());
        assertEquals("hash:Clave1234", repository.usuariosGuardados.get(0).getClave());
    }

    @Test
    void skipsAnExistingInitialAdministratorWithoutValidatingItAgain() {
        repository.emailAlreadyExists = true;

        assertFalse(useCase.registrarAdministradorInicial(
                usuarioValido().toBuilder().clave("corta").build()));
        assertTrue(repository.usuariosGuardados.isEmpty());
    }

    @Test
    void initialAdministratorStillHasToBeValid() {
        assertThrows(UsuarioInvalidoException.class, () -> useCase.registrarAdministradorInicial(
                usuarioValido().toBuilder().clave("corta").build()));
        assertThrows(UsuarioInvalidoException.class, () -> useCase.registrarAdministradorInicial(null));
    }

    private static UsuarioAutenticado solicitante(Rol rol) {
        return new UsuarioAutenticado(UUID.randomUUID(), "personal@crediya.com", rol, null);
    }

    private Usuario usuarioValido() {
        return Usuario.builder()
                .nombres("Ana")
                .apellidos("Pérez")
                .documentoIdentidad("1020304050")
                .correoElectronico("ana@example.com")
                .salarioBase(new BigDecimal("2500000"))
                .clave("Clave1234")
                .build();
    }

    @Test
    void hashesPasswordAndDefaultsRoleToClient() {
        Usuario saved = useCase.execute(usuarioValido(), solicitante(Rol.ADMIN));

        assertEquals("hash:Clave1234", saved.getClave());
        assertEquals(Rol.CLIENTE, saved.getRol());
    }

    @Test
    void keepsExplicitRoleAndDoesNotRequireDocumentForStaff() {
        Usuario saved = useCase.execute(usuarioValido().toBuilder()
                .rol(Rol.ASESOR)
                .documentoIdentidad(null)
                .build(), solicitante(Rol.ADMIN));

        assertEquals(Rol.ASESOR, saved.getRol());
    }

    @Test
    void adminCanRegisterEveryRole() {
        for (Rol rol : Rol.values()) {
            useCase.execute(usuarioValido().toBuilder()
                    .rol(rol)
                    .correoElectronico(rol.name().toLowerCase() + "@example.com")
                    .build(), solicitante(Rol.ADMIN));
        }

        assertEquals(Rol.values().length, repository.usuariosGuardados.size());
    }

    @Test
    void advisorCanRegisterClients() {
        Usuario saved = useCase.execute(usuarioValido(), solicitante(Rol.ASESOR));

        assertEquals(Rol.CLIENTE, saved.getRol());
    }

    @Test
    void advisorCannotRegisterStaff() {
        for (Rol rol : List.of(Rol.ADMIN, Rol.ASESOR)) {
            Usuario usuario = usuarioValido().toBuilder().rol(rol).build();

            assertThrows(RolNoPermitidoException.class, () -> useCase.execute(usuario, solicitante(Rol.ASESOR)));
        }
        assertEquals(0, repository.usuariosGuardados.size());
    }

    @Test
    void clientOrUnknownRequesterCannotRegisterUsers() {
        assertThrows(RolNoPermitidoException.class, () -> useCase.execute(usuarioValido(), solicitante(Rol.CLIENTE)));
        assertThrows(RolNoPermitidoException.class, () -> useCase.execute(usuarioValido(), null));
        assertEquals(0, repository.usuariosGuardados.size());
    }

    @Test
    void rejectsPasswordLongerThan72BytesEvenWithFewerCharacters() {
        Usuario usuario = usuarioValido().toBuilder().clave("ñ".repeat(37)).build();

        assertThrows(UsuarioInvalidoException.class, () -> useCase.execute(usuario, solicitante(Rol.ADMIN)));
    }

    @Test
    void rejectsFieldsLongerThanTheStorageAllows() {
        List<Usuario> usuariosInvalidos = List.of(
                usuarioValido().toBuilder().nombres("a".repeat(151)).build(),
                usuarioValido().toBuilder().apellidos("a".repeat(151)).build(),
                usuarioValido().toBuilder().direccion("a".repeat(301)).build(),
                usuarioValido().toBuilder().telefono("1".repeat(51)).build());

        for (Usuario usuario : usuariosInvalidos) {
            assertThrows(UsuarioInvalidoException.class, () -> useCase.execute(usuario, solicitante(Rol.ADMIN)));
        }
        assertEquals(0, repository.usuariosGuardados.size());
    }

    @Test
    void requiresDocumentForClients() {
        Usuario usuario = usuarioValido().toBuilder().documentoIdentidad(null).build();

        assertThrows(UsuarioInvalidoException.class, () -> useCase.execute(usuario, solicitante(Rol.ADMIN)));
    }

    @Test
    void rejectsMissingOrOutOfRangePassword() {
        List<Usuario> usuariosInvalidos = List.of(
                usuarioValido().toBuilder().clave(null).build(),
                usuarioValido().toBuilder().clave("corta").build(),
                usuarioValido().toBuilder().clave("x".repeat(73)).build());

        for (Usuario usuario : usuariosInvalidos) {
            assertThrows(UsuarioInvalidoException.class, () -> useCase.execute(usuario, solicitante(Rol.ADMIN)));
        }
    }

    private static class CifradorClavesFalso implements CifradorClaves {
        @Override
        public String cifrar(String claveEnClaro) {
            return "hash:" + claveEnClaro;
        }

        @Override
        public boolean coincide(String claveEnClaro, String claveCifrada) {
            return cifrar(claveEnClaro).equals(claveCifrada);
        }
    }

    private static class UsuarioRepositoryFalso implements UsuarioRepository {
        private boolean emailAlreadyExists;
        private boolean documentAlreadyExists;
        private final List<Usuario> usuariosGuardados = new ArrayList<>();

        @Override
        public boolean existePorCorreoElectronico(String correoElectronico) {
            return emailAlreadyExists;
        }

        @Override
        public java.util.Optional<Usuario> buscarPorCorreoElectronico(String correoElectronico) {
            return java.util.Optional.empty();
        }

        @Override
        public boolean existePorDocumentoIdentidad(String documentoIdentidad) {
            return documentAlreadyExists;
        }

        @Override
        public java.util.Optional<Usuario> buscarPorDocumentoIdentidad(String documentoIdentidad) {
            return java.util.Optional.empty();
        }

        @Override
        public Usuario guardar(Usuario usuario) {
            usuariosGuardados.add(usuario);
            return usuario;
        }
    }
}
