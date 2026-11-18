package co.com.crediya.usecase.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.com.crediya.model.autenticacion.UsuarioAutenticado;
import co.com.crediya.model.autenticacion.exception.AccesoDenegadoException;
import co.com.crediya.model.usuario.Rol;
import co.com.crediya.model.usuario.Usuario;
import co.com.crediya.model.usuario.exception.UsuarioNoEncontradoException;
import co.com.crediya.model.usuario.gateways.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ConsultarUsuarioPorDocumentoUseCaseTest {
    private static final String DOCUMENTO = "1020304050";

    private final UsuarioRepository repository = new UsuarioRepository() {
        @Override
        public boolean existePorCorreoElectronico(String correoElectronico) {
            return false;
        }

        @Override
        public Optional<Usuario> buscarPorCorreoElectronico(String correoElectronico) {
            return Optional.empty();
        }

        @Override
        public boolean existePorDocumentoIdentidad(String documentoIdentidad) {
            return DOCUMENTO.equals(documentoIdentidad);
        }

        @Override
        public Optional<Usuario> buscarPorDocumentoIdentidad(String documentoIdentidad) {
            return DOCUMENTO.equals(documentoIdentidad)
                    ? Optional.of(Usuario.builder().documentoIdentidad(DOCUMENTO).build())
                    : Optional.empty();
        }

        @Override
        public Usuario guardar(Usuario usuario) {
            return usuario;
        }
    };
    private final ConsultarUsuarioPorDocumentoUseCase useCase = new ConsultarUsuarioPorDocumentoUseCase(repository);

    @Test
    void staffFindsAnyRegisteredDocument() {
        for (Rol rol : List.of(Rol.ADMIN, Rol.ASESOR)) {
            assertEquals(DOCUMENTO, useCase.execute(DOCUMENTO, solicitante(rol, null)).getDocumentoIdentidad());
        }
    }

    @Test
    void clientFindsOnlyTheirOwnDocument() {
        assertEquals(DOCUMENTO,
                useCase.execute(DOCUMENTO, solicitante(Rol.CLIENTE, DOCUMENTO)).getDocumentoIdentidad());
        assertThrows(AccesoDenegadoException.class,
                () -> useCase.execute(DOCUMENTO, solicitante(Rol.CLIENTE, "5555555")));
    }

    @Test
    void deniesBeforeRevealingWhetherTheDocumentExists() {
        assertThrows(AccesoDenegadoException.class,
                () -> useCase.execute("999999", solicitante(Rol.CLIENTE, DOCUMENTO)));
        assertThrows(AccesoDenegadoException.class, () -> useCase.execute(DOCUMENTO, null));
    }

    @Test
    void throwsWhenDocumentDoesNotExist() {
        assertThrows(UsuarioNoEncontradoException.class,
                () -> useCase.execute("999999", solicitante(Rol.ASESOR, null)));
    }

    private static UsuarioAutenticado solicitante(Rol rol, String documento) {
        return new UsuarioAutenticado(UUID.randomUUID(), "usuario@crediya.com", rol, documento);
    }
}
