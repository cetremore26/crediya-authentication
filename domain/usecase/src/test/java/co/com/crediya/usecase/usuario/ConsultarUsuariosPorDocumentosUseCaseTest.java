package co.com.crediya.usecase.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.com.crediya.model.autenticacion.UsuarioAutenticado;
import co.com.crediya.model.autenticacion.exception.AccesoDenegadoException;
import co.com.crediya.model.usuario.Rol;
import co.com.crediya.model.usuario.Usuario;
import co.com.crediya.model.usuario.gateways.UsuarioRepository;
import co.com.crediya.model.usuario.exception.UsuarioInvalidoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class ConsultarUsuariosPorDocumentosUseCaseTest {
    private static final UsuarioAutenticado ASESOR =
            new UsuarioAutenticado(UUID.randomUUID(), "asesor@crediya.com", Rol.ASESOR, null);
    private final List<Collection<String>> consultas = new ArrayList<>();
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
            return false;
        }

        @Override
        public Optional<Usuario> buscarPorDocumentoIdentidad(String documentoIdentidad) {
            throw new AssertionError("La consulta por lote debe hacer una sola consulta.");
        }

        @Override
        public List<Usuario> buscarPorDocumentosIdentidad(Collection<String> documentosIdentidad) {
            consultas.add(documentosIdentidad);
            return documentosIdentidad.stream()
                    .filter(documento -> "111111".equals(documento) || "222222".equals(documento))
                    .map(documento -> Usuario.builder().documentoIdentidad(documento).build())
                    .toList();
        }

        @Override
        public Usuario guardar(Usuario usuario) {
            return usuario;
        }
    };
    private final ConsultarUsuariosPorDocumentosUseCase useCase = new ConsultarUsuariosPorDocumentosUseCase(repository);

    @Test
    void returnsKnownUsersWithoutDuplicatesAndSkipsUnknown() {
        List<Usuario> usuarios = useCase.execute(List.of("111111", " 222222 ", "111111", "999999"), ASESOR);

        assertEquals(2, usuarios.size());
        assertEquals(List.of(List.of("111111", "222222", "999999")), consultas);
    }

    @Test
    void ignoresBlankDocumentsWithoutQuerying() {
        assertEquals(List.of(), useCase.execute(java.util.Arrays.asList(" ", null), ASESOR));
        assertEquals(List.of(), consultas);
    }

    @Test
    void rejectsEmptyAndOversizedRequests() {
        List<String> tooMany = IntStream.range(0, 101).mapToObj(String::valueOf).toList();

        assertThrows(UsuarioInvalidoException.class, () -> useCase.execute(null, ASESOR));
        assertThrows(UsuarioInvalidoException.class, () -> useCase.execute(List.of(), ASESOR));
        assertThrows(UsuarioInvalidoException.class, () -> useCase.execute(tooMany, ASESOR));
    }

    @Test
    void onlyStaffCanListUsers() {
        UsuarioAutenticado admin = new UsuarioAutenticado(UUID.randomUUID(), "a@crediya.com", Rol.ADMIN, null);
        UsuarioAutenticado cliente =
                new UsuarioAutenticado(UUID.randomUUID(), "c@crediya.com", Rol.CLIENTE, "111111");

        assertEquals(1, useCase.execute(List.of("111111"), admin).size());
        assertThrows(AccesoDenegadoException.class, () -> useCase.execute(List.of("111111"), cliente));
        assertThrows(AccesoDenegadoException.class, () -> useCase.execute(List.of("111111"), null));
    }
}
